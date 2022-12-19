#!/usr/bin/env bb
;; emulate
;; curl -c cookie.txt -X POST -d "id=16" https://wc.kohhoh.jp/api/meas
;;
;; FIXME: `./users.clj | jq` does not look like httpie's outputs.

(ns weights
  (:require [babashka.curl :as curl]
            [cheshire.core :as json]))

(def weights-url "https://wc.kohhoh.jp/api/meas")
(def cookie "cookie.txt")

(def params (str "id=" 16 "&meastype=1&lastupdate=" "2022-09-30"))

;; need refresh-token before processing

(-> (curl/post weights-url
               {:raw-args ["-b" cookie "-d" params]
                :folow-redirects false}))
