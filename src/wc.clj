(ns wc
  "fetch meas via https://wc.kohhoh.jp"
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]
   [tokens :refer [login-success? refresh-all!]]))

(def wc "https://wc.kohhoh.jp")
(def cookie "cookie.txt")

;; short-hand functions
;; conversations to wc.kohhoh.jp require cookie.
(defn curl-get [url & params]
  (curl/get url {:raw-args (vec (concat ["-b" cookie] params))}))

(defn curl-post [url & params]
  (curl/post url {:raw-args (vec (concat ["-b" cookie] params))}))

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

;; WITHINGS が間違い！
;; meastypes ではなく、meastype
;; meastypes=1,4,12 は invalid parameter error を起こす。
;; meastype, meastypes に何も指定しなければ withings にある全部のデータを返す。
;; 本当か？エラーになる。
(defn get-meas-one
  ([id day1]
   (let [params (str "id=" id "&lastupdate=" day1)]
     (get-meas-with params)))
  ([id day1 day2]
   (let [params (str "id=" id "&startdate=" day1 "&enddate=" day2)]
     (get-meas-with params))))

(defn get-meas-test
  []
  (login-success?)
  (refresh-all!)
  (get-meas-one 51 "2022-12-10"))

(get-meas-test)
