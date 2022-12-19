#!/usr/bin/env bb
(ns src.withings-cache
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]))

;;(def wc-url "localhost:3000")
(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

;; mariadb
(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

;; authentication
(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [params (str "login=" admin "&password=" password)
        api (str wc "/")]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(comment
  (login)
  :rcf)

;; users
(defn fetch-users
  "fetch users via withing-client, return the user json"
  []
  (let [api (str wc "/api/users")]
    ;; (println "api" api)
    (-> (curl/get api {:raw-args ["-b" cookie]})
        :body
        (json/parse-string true)
        vec)))

(comment
  (def users (fetch-users))
  :rcf)

;; tokens
(defn refresh!
  "Refresh user id's refresh token."
  [id]
  (let [api (str wc "/api/token/" id "/refresh")]
    ;; (println "api" api)
    (curl/post api {:raw-args ["-b" cookie]})))

(comment
  (refresh! 16)
  (refresh! 32)
  :rcf)

(defn refresh-all!
  "Refresh all available user tokens."
  []
  (let [api (str wc "/api/tokens/refresh-all")]
    (curl/post api {:raw-args ["-b" cookie]})))

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
;; Store fetched data in database.
