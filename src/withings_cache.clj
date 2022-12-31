(ns withings-cache
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;;; tokens
(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

(def users (atom nil))

(defn login
  "login. if success, updates cookie and returns 302."
  []
  (let [api (str wc "/")
        params (str "login=" admin "&password=" password)]
    (curl/post api {:raw-args ["-c" cookie "-d" params]
                    :follow-redirects false})))

(defn curl-get [url & params]
  (curl/get url {:raw-args (vec (concat ["-b" cookie] params))}))

(defn curl-post [url & params]
  (curl/post url {:raw-args (vec (concat ["-b" cookie] params))}))

(defn fetch-users
  "fetch users via withing-client,
   return the users data in json format."
  []
  (-> (curl-get (str wc "/api/users"))
      :body
      (json/parse-string true)
      vec))

(comment
  (reset! users (fetch-users))
  :rcf)

(defn refresh!
  "Refresh user id's refresh token."
  [id]
  ;; (println "refresh!" id)
  (curl-post (str wc "/api/token/" id "/refresh")))

;; pmap でスピードアップ。
(defn refresh-all!
  "use old users map internaly, returns refreshed user map.
   becore fetch-users, login is required."
  []
  (let [ids (->> (filter :valid (fetch-users))
                 (map :id))]
    (doall (pmap refresh! ids))))

(defn refresh-all!-test
  [id]
  (login)
  (refresh-all!)
  (reset! users (fetch-users))
  (->> @users
       (filter #(= id (:id %)))
       first
       :access))
(comment
  (refresh-all!-test 27)
  :rcf)
