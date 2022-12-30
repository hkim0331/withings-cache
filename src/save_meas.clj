(ns save-meas
  (:require
   ;;[babashka.curl :as curl]
   [babashka.pods :as pods]
   ;;[cheshire.core :as json]
   [clojure.java.shell :refer [sh]]
   [clojure.math  :refer [pow]]
   [clojure.string :as str]
   [tokens :refer [refresh-all!]]
   [wc]))

(pods/load-pod 'org.babashka/mysql "0.1.1")
(require '[pod.babashka.mysql :as mysql])
(def db {:dbtype   "mysql"
         :host     "localhost"
         :port     3306
         :dbname   "withings"
         :user     (System/getenv "MYSQL_USER")
         :password (System/getenv "MYSQL_PASSWORD")})

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
