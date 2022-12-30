(ns src.via_wc
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.math  :refer [pow]]
   [clojure.string :as str]
   [src.tokens :refer [login-success? refresh-all! fetch-users]]))

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
  (login-success?)
  (refresh-all!)
  (get-weight 51 "2022-12-01")
  (get-meas-all 51 "2022-12-01")
  :rcf)
