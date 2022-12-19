#!/usr/bin/env bb
;; emulate
;; curl -X POST -c cookie.txt -d "login=${login}&password=${password}" https://wc.kohhoh.jp/
(ns login
  (:require [babashka.curl :as curl]))

;;(def wc-url "localhost:3000/")
(def wc-url "https://wc.kohhoh.jp/")
(def cookie "cookie.txt")
(def login (System/getenv "WC_LOGIN"))
(def password (System/getenv "WC_PASSWORD"))
(def params (str "login=" login "&password=" password))
;;(println "params" params)

(curl/post wc-url {:raw-args ["-c" cookie "-d" params]
                   :follow-redirects false})
