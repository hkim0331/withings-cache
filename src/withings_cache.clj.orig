(ns withings-cache
  (:require
   [babashka.curl :as curl]
   [babashka.pods :as pods]
   [cheshire.core :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.math  :refer [pow]]
   [clojure.string :as str]))

(pods/load-pod 'org.babashka/mysql "0.1.1")
(require '[pod.babashka.mysql :as mysql])
(def db {:dbtype   "mysql"
         :host     "localhost"
         :port     3306
         :dbname   "withings"
         :user     (System/getenv "MYSQL_USER")
         :password (System/getenv "MYSQL_PASSWORD")})

(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; authentication
(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [api (str wc "/")
        params (str "login=" admin "&password=" password)]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(defn login-success?
  []
  (= 302 (:status (login))))

;; `fetch-users` requires login.
(login-success?)

;; short-hand functions
;; conversations to wc.kohhoh.jp require cookie.
(defn curl-get [url & params]
  (curl/get url {:raw-args (vec (concat ["-b" cookie] params))}))

(defn curl-post [url & params]
  (curl/post url {:raw-args (vec (concat ["-b" cookie] params))}))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; users
(defn fetch-users
  "fetch users via withing-client,
   return the users data in json format."
  []
  (-> (curl-get (str wc "/api/users"))
      :body
      (json/parse-string true)
      vec))

(comment
  (fetch-users)
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; tokens
(defn refresh!
  "Refresh user id's refresh token."
  [id]
  (curl-post (str wc "/api/token/" id "/refresh")))

;; pmap でスピードアップ。
(defn refresh-all!
  "use old users map internaly, returns refreshed user map.
   becore fetch-users, login is required."
  []
  (and
   (login-success?)
   (->> (filter :valid (fetch-users))
        (map :id)
        (pmap refresh!))
   ;; FIXME: this returns old users.
   #_(fetch-users)))

(comment
  (def users (refresh-all!))
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get meas
;; direct download from withings
;; need users
(def withings "https://wbsapi.withings.net/measure")

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

(defn access-token [id users]
  (->> users
       (filter #(= id (:id %)))
       first
       :access))


(defn withings-get-meas
  "users must be set before calling this function."
  [id date users]
  (let [token  (access-token id users)
        params (str "action=getmeas&lastupdate=" (to-unix-time date))]
    (println "id:" id)
    (println "access token:" token)
    (println "params:" params)
    (->
     (curl/get withings
               {:raw-args ["-H" (str "Authorization: Bearer " token)
                           "-d" params]})
     :body
     (json/parse-string true)
     (get-in [:body :measuregrps]))))

(defn withings-test
  []
  (let [_ (refresh-all!)
        users (fetch-users)]
    [(withings-get-meas 51 "2022-09-01 00:00:00" users)
     (withings-get-meas 51 "2022-12-20 00:00:00" users)]))

(comment
  (def ret (withings-test))
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get meas
;; get via https://wc.kohhoh.jp
(defn get-meas-with
  [params]
  (println "get-meas-with" params)
  (-> (curl-post (str wc "/api/meas") "-d" params)
      :body
      (json/parse-string true)
      vec))

(defn get-meas
  ([id type day1]
   (let [params (str "id=" id "&meastype=" type "&lastupdate=" day1)]
     (get-meas-with params)))
  ([id type day1 day2]
   (let [params
         (str "id=" id "&meastype=" type "&startdate=" day1 "&enddate=" day2)]
     (get-meas-with params))))

(defn get-weight
  "weight: meastype=1"
  ([id lastupdate]
   (get-meas id 1 lastupdate))
  ([id startdate enddate]
   (get-meas id 1 startdate enddate)))

;; WITHINGS が間違い！
;; meastypes ではなく、meastype
;; meastypes=1,4,12 は invalid parameter error を起こす。
;; meastype, meastypes に何も指定しなければ withings にある全部のデータを返す。
;; 本当か？エラーになる。
(defn get-meas-all
  ([id day1]
   (let [params (str "id=" id "&lastupdate=" day1)]
     (get-meas-with params)))
  ([id day1 day2]
   (let [params (str "id=" id "&startdate=" day1 "&enddate=" day2)]
     (get-meas-with params))))

(comment
  (login)
  (def users (refresh-all!))
  (get-weight 27 "2022-12-01")
  (def record (get-meas-all 27 "2022-12-01")) ;; fixed at 0.15.5
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; save meas
;; FIXME: meas->float returns 93.60000000000001
(defn meas->float
  [{:keys [value unit]}]
  (* value (pow 10 unit)))

;; FIXME: must uniq against (id, created)
(defn save-one!
  [id {:keys [created measures]}]
  (let [meas (first measures) ;; <-
        type (:type meas)]
    ;; (println "save-one!" id type created (meas->float meas))
    (mysql/execute!
     db
     ["insert into meas
       (user_id, type, measure, created)
       values (?,?,?, from_unixtime(?))"
      id
      type
      (meas->float meas)
      created])))

(defn save!
  [id meas]
  (doseq [mea meas]
    (save-one! id mea)))

(defn save-meas!
  ([id type day1]
   (save! id (get-meas id type day1)))
  ([id type day1 day2]
   (save! id (get-meas id type day1 day2))))

(defn save-weight!
  ([id lastupdate]
   (save-meas! id 1 lastupdate))
  ([id startdate enddate]
   (save-meas! id 1 startdate enddate)))

(comment
  (save-weight! 16 "2022-09-01")
  :rcf)

(defn init-db-weight
  "init by weights data."
  [last-update]
  (let [users (refresh-all!)]
    (doseq [{:keys [id]} users]
      (println id)
      ;; should change to `save-multi!`
      (save-weight! id last-update))))

(comment
  (init-db-weight "2022-09-01")
  :rcf)
