#!/usr/bin/env bb
(ns src.withings-cache
  (:require
   [babashka.curl :as curl]
   [babashka.pods :as pods]
   [cheshire.core :as json]
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

;; users
(defn fetch-users
  "fetch users via withing-client, return the user json"
  []
  (-> (curl-get (str wc "/api/users"))
      :body
      (json/parse-string true)
      vec))

(comment
  (def users (fetch-users))
  users
  )
;; tokens
(defn refresh!
  "Refresh user id's refresh token."
  [id]
  (curl-post (str wc "/api/token/" id "/refresh")))

(comment
  (refresh! 16)
  (refresh! 51)
  :rcf)

;; (defn refresh-all!
;;   "Refresh all available user tokens."
;;   []
;;   (curl-post (str wc "/api/tokens/refresh-all")))

;; pmap でスピードアップ。
(defn refresh-all-pmap!
  [users]
  (->> (filter :valid users)
       (map :id)
       (pmap refresh!)))

(comment
  (refresh-all-pmap! (fetch-users))
  :rcf)

;; get meas
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

(comment
  (login)
  (get-meas 27 "1,3,5" "2022-12-01") ;; 不適切な番号でもフェッチする。
  (get-weight 16 "2022-10-31" "2022-11-20")
  :rcf)

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
  (get-weight 27 "2022-12-01")
  (def record (get-meas-all 27 "2022-12-01")) ;; fixed at 0.15.5
  record
  (count record)
  :rcf)

;;;;;;;;;;;;;;
;; save meas
;; FIXME: meas->float returns 93.60000000000001
(defn meas->float
  [{:keys [value unit] :as params}]
  ;; (println value unit params)
  (* value (pow 10 unit)))

;; FIXME: 複数の meas には対応していない。
;; FIXME: must uniq agains (id, created)
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
  (let [users (filter :valid (fetch-users))]
    (refresh-all-pmap! users)
    (doseq [{:keys [id]} users]
      (println id)
      ;; should change to `save-multi!`
      (save-weight! id last-update))))

(comment
  (init-db-weight "2022-09-01")
  :rcf)

;; 48
;; 32
;; 17
;; ; clojure.lang.ExceptionInfo: babashka.curl: status 400 src.withings-cache /Volumes/RAM_DISK/withings-cache/src/withings_cache.clj:44:3
;; ; Evaluation of file withings_cache.clj failed: class clojure.lang.ExceptionInfo
