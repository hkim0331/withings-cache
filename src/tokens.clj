(ns tokens
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]))

(def users (atom nil))

(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))

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

(comment
  (login-success?)
  :rcf)

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
  (fetch-users)
  :rcf)

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
  (refresh-all!)
  (reset! users (fetch-users))
  (->> @users
       (filter #(= 51 (:id %)))
       first
       :access)
  :rcf)
