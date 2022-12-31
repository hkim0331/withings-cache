(ns keep.directly-withings
  "directry fetch meas from withings"
  (:require
   [babashka.curl :as curl]
   [cheshire.core :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.string :as str]
   [keep.tokens :refer [login refresh-all! fetch-users]]))

(def withings "https://wbsapi.withings.net/measure")
;; (if (login)
;;   (refresh-all!)
;;   (println "can not login"))

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

(defn access-token [id]
  (->> (fetch-users)
       (filter #(= id (:id %)))
       first
       :access))

(defn get-meas-one
  "users must be set before calling this function."
  [id date]
  (let [token  (access-token id)
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

;; FIXME: 関数にすると失敗する。
;;        refresh-all! が完了しない前に access-token が呼ばれている感じ。
(defn get-meas-test []
  (login)
  (refresh-all!)
  (get-meas-one 51 "2022-12-10 00:00:00"))

(comment
  ;; returns nil
  (get-meas-test)
  :rcf)
