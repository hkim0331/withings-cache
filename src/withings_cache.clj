(ns src.withings-cache
  (:require
   [babashka.curl :as curl]
   [babashka.pods :as pods]
   [cheshire.core :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.math  :refer [pow]]))

(pods/load-pod 'org.babashka/mysql "0.1.1")
(require '[pod.babashka.mysql :as mysql])
(def db {:dbtype   "mysql"
         :host     "localhost"
         :port     3306
         :dbname   "withings"
         :user     (System/getenv "MYSQL_USER")
         :password (System/getenv "MYSQL_PASSWORD")})

;; debug
;;(def wc "http://localhost:3000")
(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

;; withing-client
(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

;; authentication
(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [api (str wc "/")
        params (str "login=" admin "&password=" password)]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(comment
  (:status (login))
  :rcf)

;; short-hand functions
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

(comment
  (refresh! 16)
  (refresh! 51)
  :rcf)

;; pmap でスピードアップ。
(defn refresh-all!
  "use old users map internaly,
   returns refreshed user map."
  []
  (->> (filter :valid (fetch-users))
       (map :id)
       (pmap refresh!))
  (fetch-users))

(comment
  (refresh-all!)
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get meas

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; under constrution
;; direct download from withings
(defn to-unix-time
  [datetime]
  (:out (sh "date" "-d" datetime "+%s")))

(comment
  (to-unix-time "2022-12-20 12:34:56")
  :rcf)

(def users (fetch-users))
(defn access-token [id users]
  (->> users
       (filter #(= 51 (:id %)))
       first
       :access))

(comment
  (access-token 51 users)
  )

;; curl \
;;   --header "Authorization: Bearer ${token}" \
;;   --data "action=getmeas&lastupdate=1669849930" \
;;   'https://wbsapi.withings.net/measure'

(def withings "https://wbsapi.withings.net/measure")case

(defn withings-get-meas
  "users must be set before calling this function."
  [id date users]
  (let [token (access-token id users)
        params (str "action=getmeas&lastdate=" (to-unix-time date))]
    (curl/get withings {:raw-args ["-H" (str "Authorization: Bearer " token)
                                   "-d" params]})))

(withings-get-meas 51 "2022-12-01" users)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
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
  (withing-get-meas 27 "2022-12-01" users)
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
