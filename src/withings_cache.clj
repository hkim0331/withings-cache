(ns withings-cache
  (:require
   [babashka.curl :as curl]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]
   [cheshire.core :as json]))

;;;
(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")
(def admin    (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))
(def users (atom nil))
;;;

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
  (curl/get url {:raw-args (vec (concat ["-b" cookie] params))}))

(defn curl-post [url & params]
  (curl/post url {:raw-args (vec (concat ["-b" cookie] params))}))

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
  (let [ids (->> (fetch-users)
                 (filter :valid)
                 (map :id))]
    (doall (pmap refresh! ids))))

(defn access-code [id]
  (->> @users
       (filter #(= id (:id %)))
       first
       :access))

(comment
  (access-code 51)
  :rcf)

(defn refresh-all!-test
  [id]
  (login)
  (refresh-all!)
  (reset! users (fetch-users))
  (access-code id))

(comment
  (refresh-all!-test 27)
  :rcf)

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; save meas

;; under construction
(defn save-meas!
  [id data]
  (println "save-meas! id:" id)
  (doseq [d data]
    (println "  created:" (from-unix-time (:created d)))
    (println "  measures:" (:measures d))))

;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;;
;; get meas
;; get via https://wc.kohhoh.jp
;; over version exists. `directry_withings.clj`.
(defn get-meas-with
  [params]
  (println "get-meas-with" params)
  (-> (curl-post (str wc "/api/meas") "-d" params)
      :body
      (json/parse-string true)
      vec))

;; not using
;; (defn get-meas
;;   ([id type day1]
;;    (let [params (str "id=" id "&meastype=" type "&lastupdate=" day1)]
;;      (get-meas-with params)))
;;   ([id type day1 day2]
;;    (let [params
;;          (str "id=" id "&meastype=" type "&startdate=" day1 "&enddate=" day2)]
;;      (get-meas-with params))))

;; WITHINGS が間違い！
;; meastypes ではなく、meastype
;; meastypes=1,4,12 は invalid parameter error を起こす。
;; meastype, meastypes に何も指定しなければ withings にある全部のデータを返す。
;; 本当か？
;; day1, day2 は wc が unix time に直すので、2022-12-20 形式で渡す。
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
  (get-meas-one 51 "2022-12-20")
  :rcf)

;; function name init-db?
(defn get-meas-all
  [date]
  (login)
  (refresh-all!)
  (reset! users (fetch-users))
  (doseq [{:keys [id]} @users]
    (save-meas! id (get-meas-one id date))))

(comment
  (save-meas! 51 (get-meas-one 51 "2022-12-01"))
  (get-meas-all "2022-12-01")
  ; clojure.lang.ExceptionInfo: babashka.curl: status 400 withings-cache /Users/hkim/clojure/withings-cache/src/withings_cache.clj:28:3
  :rcf)
