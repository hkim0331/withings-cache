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
         :user     "user"
         :password "secret"})

;;(def wc-url "localhost:3000")
(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

;; withing-client
(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

;; withings database


;; authentication
(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [api (str wc "/")
        params (str "login=" admin "&password=" password)]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(comment
  (login)
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
  :rcf)

;; tokens
(defn refresh!
  "Refresh user id's refresh token."
  [id]
  (curl-post (str wc "/api/token/" id "/refresh")))

(comment
  (refresh! 16)
  ;; (refresh! 32)
  :rcf)

(defn refresh-all!
  "Refresh all available user tokens."
  []
  (curl-post (str wc "/api/tokens/refresh-all")))

(comment
  ;; 32 山﨑悠翔 がリフレッシュできない。ひとまずオフ。
  (refresh-all!)
  :rcf)

;; pmap でスピードアップ。
(defn refresh-all-pmap!
  [users]
  (->> (filter :valid users)
       (map :id)
       (pmap refresh!)))

(comment
  (refresh-all-pmap! users)
  :rcf)

;; get meas
(defn fetch-meas-with
  [params]
  (-> (curl-post (str wc "/api/meas") "-d" params)
      :body
      (json/parse-string true)
      vec))

(defn fetch-meas
  ([id type d1]
   (let [params (str "id=" id "&meastype=" type "&lastupdate=" d1)]
     (fetch-meas-with params)))
  ([id type d1 d2]
   (let [params
         (str "id=" id "&meastype=" type "&startdate=" d1 "&enddate=" d2)]
     (fetch-meas-with params))))

(defn fetch-weight
  "weight: meastype=1"
  ([id lastupdate]
   (fetch-meas id 1 lastupdate))
  ([id startdate enddate]
   (fetch-meas id 1 startdate enddate)))

(comment
  (fetch-weight 16 "2022-10-30")
  (fetch-weight 16 "2022-10-31" "2022-11-20")
  :rcf)

;;;;;;;;;;;;;;
;; save meas
;; FIXME: meas->float returns 93.60000000000001
(defn meas->float
  [{:keys [value unit] :as params}]
  ;; (println value unit params)
  (* value (pow 10 unit)))

;; FIXME: 複数の meas には対応していない。
;; FIXME: must uniq same id, same created
(defn save-one!
  [id {:keys [created measures]}]
  (let [meas (first measures) ;; <-
        type (:type meas)]
    (println "save-one!" id type created (meas->float meas))
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
  ([id type d1]
   (save! id (fetch-meas id type d1)))
  ([id type d1 d2]
   (save! id (fetch-meas id type d1 d2))))

(defn save-weight!
  ([id lastupdate]
   (save-meas! id 1 lastupdate))
  ([id startdate enddate]
   (save-meas! id 1 startdate enddate)))

(comment
  (save-weight! 16 "2022-09-01")
  )
