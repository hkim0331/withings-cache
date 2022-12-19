#!/usr/bin/env bb
;; emulate
;; curl -c cookie.txt https://wc.kohhoh.jp/api/users
;;
;; FIXME: `./users.clj | jq` does not look like httpie's outputs.

(ns users
  (:require [babashka.curl :as curl]
            [cheshire.core :as json]))

(def users-url "https://wc.kohhoh.jp/api/users")
(def cookie "cookie.txt")

(-> (curl/get users-url {:raw-args ["-b" cookie]})
    :body
    (json/parse-string true)
    vec)
