(ns main
  (:require
   [babashka.curl :as curl]
   [babashka.pods :as pods]
   [clojure.java.shell :refer [sh]]
   [clojure.math :refer [pow]]
   [clojure.string :as str]
   [clojure.tools.logging :as log]
   [cheshire.core :as json]))

(def ^:private version "v1.11.126")

(def wc (System/getenv "WC"))
(def cookie "cookie.txt")
(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))
(def users (atom nil))

(pods/load-pod 'org.babashka/mysql "0.1.2")

(require '[pod.babashka.mysql :as mysql])

(def db {:dbtype   "mysql"
         :host     "localhost"
         :port     3306
         :dbname   "withings"
         :user     (System/getenv "MYSQL_USER")
         :password (System/getenv "MYSQL_PASSWORD")})

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; utils
(defn to-unix-time
  [datetime]
  (-> (sh "date" "-d" datetime "+%s")
      :out
      str/trim-newline))

(defn from-unix-time
  [n]
  (-> (sh "date" "-d" (str "@" n) "+%Y/%m/%d %T")
      :out
      str/trim-newline))

(defn curl-get [url & params]
  (let [args (vec (concat ["-b" cookie] params))]
    (log/debug "curl-get" url ":raw-args" args)
    (curl/get url {:raw-args args})))

(defn curl-post [url & params]
  (let [args (vec (concat ["-b" cookie] params))]
    (log/debug "curl-post" url ":raw-args" args)
    (curl/post url {:raw-args args})))

(comment
  ;; ここ？
  (vec (concat ["-b" cookie]))
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; tokens
(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [api (str wc "/")
        params (str "login=" admin "&password=" password)]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(defn fetch-users
  "fetch users via withing-client,
   return the users data in json format.
   (fetch-users true) returns only valid users."
  [& valid?]
  (let [ret (-> (curl-get (str wc "/api/users"))
                :body
                (json/parse-string true)
                vec)]
    (if valid?
      (filter :valid ret)
      ret)))

(comment
  (reset! users (fetch-users))
  (reset! users (fetch-users true))
  :rcf)

(defn refresh!
  "Refresh user `id`s refresh token."
  [id]
  (log/debug "refresh!" id)
  (curl-post (str wc "/api/token/" id "/refresh")))

;; pmap でスピードアップ。
(defn refresh-all!
  "use old users map internaly, returns refreshed user map.
   before fetch-users, login is required."
  []
  (log/debug "refresh-all!")
  (let [ids (->> (fetch-users)
                 (filter :valid)
                 (map :id))]
    ;; (doall (pmap refresh! ids))
    ;; choose non-parallel way.
    (doseq [id ids]
      (refresh! id))))

(defn access-code [id]
  (->> @users
       (filter #(= id (:id %)))
       first
       :access))

(defn refresh-all!-test
  [id]
  (login)
  (refresh-all!)
  (reset! users (fetch-users true))
  (access-code id))

(comment
  (refresh-all!-test 27)
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; save meas
(defn meas->float
  [{:keys [value unit]}]
  (* value (pow 10 unit)))

;; FIXME: must uniq against (id, created)
(defn save-meas-one!
  "id: user_id
   date: 1671704684
   measures: [{:value 54600, :type 1, :unit -3, ...}
              {:value 1100, :type 8, :unit -2, ...} ..]"
  [id date measures]
  (log/debug "save-meas-one! id:" id "date:" date)
  (doseq [{:keys [type] :as meas} measures]
    (log/debug "  type:" type "float value =>" (meas->float meas))
    (mysql/execute!
     db
     ["insert into meas
       (user_id, type, measure, created)
       values (?,?,?, from_unixtime(?))"
      id
      type
      (meas->float meas)
      date])))

(defn save-meas!
  [id data]
  (log/debug "save-meas! id:" id)
  (doseq [d data]
    (save-meas-one! id (:created d) (:measures d))))

(defn delete-all!
  []
  (mysql/execute! db ["delete from meas"]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get meas
;; get via https://wc.kohhoh.jp
;; over version exists. `directry_withings.clj`.
(defn get-meas-with
  [params]
  (log/debug "get-meas-with" params)
  (-> (curl-post (str wc "/api/meas") "-d" params)
      :body
      (json/parse-string true)
      vec))

;; WITHINGS が間違い！
;; meastypes ではなく、meastype
;; meastypes=1,4,12 は invalid parameter error を起こす。
;; meastype, meastypes に何も指定しなければ withings にある全部のデータを返す。
;; 本当か？
;; day1, day2 は wc が unix time に直すので、"2022-12-20" 形式で渡す。
;; FIXME: id が存在しないユーザであれば nil を返してよい。
(defn get-meas-one
  ([id day1]
   (let [params (str "id=" id "&lastupdate=" day1)]
     (get-meas-with params)))
  ([id day1 day2]
   (let [params (str "id=" id "&startdate=" day1 "&enddate=" day2)]
     (get-meas-with params))))

(comment
  (login)
  (refresh-all!)
  (get-meas-one 51 "2022-12-01")
  ;; FIXME: this must be error.
  ;;        `invalid user` or `user does not exist`.
  (get-meas-one 16 "2022-12-01")
  :rcf)

(defn get-save-meas-all
  "Get all user meas since `lastupdate` via wc.kohhoh.jp,
   save them in `withings.meas` table."
  [lastupdate]
  (log/debug "get-save-meas-all" lastupdate)
  (login)
  (refresh-all!)
  (reset! users (fetch-users true))
  (doseq [{:keys [id]} @users]
    (save-meas! id (get-meas-one id lastupdate))))

(comment
  (save-meas! 27 (get-meas-one 27 "2022-12-20"))
  (delete-all!)
  (get-save-meas-all "2022-12-10")
  :rcf)

(defn init-meas
  "initialize meas table"
  [& args]
  (delete-all!)
  (if (nil? args)
    (get-save-meas-all "2022-09-01")
    (get-save-meas-all (first args))))

(comment
  (init-meas)
  :rcf)

;;; updating
;;; first delete, then add to avoid data duplications.
(defn delete-meas-since
  "delete meas from `date`."
  [date]
  (log/debug "delete-meas-since" date)
  (mysql/execute!
   db
   ["delete from meas where created >= ?" date]))

(defn update-meas-since
  "deleting meas from date, then
   fetch meas and save them."
  [date]
  (log/debug "update-meas-since" date)
  ;; (delete-meas-since date)
  (get-save-meas-all date))

(defn- today
  "returns a string 'yyyy-mm-dd'"
  []
  (str/trim-newline (:out (sh "date" "+%F"))))

(defn print-env []
  (println "wc" wc)
  (println "cookie" (slurp "cookie.txt"))
  (println "admin"  admin))

;; FIXME: simpler!
(defn -main
  [& [arg]]
  (update-meas-since (or arg (today)))
  #_(cond
      (nil? arg) (update-meas-since (today))
      (= "version" arg) (println version)
      (= "env" arg) (print-env)
      (= "help" arg) (println "bb -m main [version env help yyyy-mm-dd]")
      :else (update-meas-since arg)))
