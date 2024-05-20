# withings-cache

## Unreleased
- use babashka task?
- hyperfiddle.rcf
- direct downloading from withings. access tokens should be from wc.kohhoh.
- require java.time fails in babashka.
- how to call `init-meas` from shell?
- periodically call `update-meas-today`. may be 'systemctl timer'.
- babashka.curl:
  NOTE: This library is mostly replaced by babashka.http-client which is mostly API-compatible with babashka.curl. The babashka.http-client library is built-in as of babashka version 1.1.171.

## v1.2.131 / 2024-05-20
- org.babashka/mysql 0.1.2
- fix main.clj (first arg) -> arg
- chose non-par way.

## v1.11.126 / 2024-05-19
- m24 can fetch old data.

## v1.10.120 / 2024-05-19
- improve main/-main
- defined main/today
- added kohhoh id_rsa.pub to github.com/hkim0331

## v1.9.115 / 2024-05-19
### Changed
- bump-version.sh updates `CHANGELOG.md` and `bb/main.clj`.


<!-- restart developing -->

## 0.8.22 - 2023-01-04
- created systemd/withings-cache.{service,timer}
- make service
- fix typo OnCalener -> OnCalendar
- EnvironmetFile's path must be aboslute.

## 0.7.18 - 2022-12-31
- defined `update-meas-today`.
  need `str/trim-newline` to use the output of `date +%F` as arguments.
- `init-db` is not the -main. `update-meas-today` is it.
- renamed `init-db` to `init-meas`.

## 0.7.17 - 2022-12-31
- log/debug
- renamed `get-meas-all` to `get-save-meas-all`.
- defined `delete-meas-since` and `update-meas-since`.

## 0.7.16 - 2022-12-31
- added Makefile and migrations/README.md
- worked on wc.kohhoh.jp
- bb -m main
- bb -m main yyyy-mm-dd

## 0.7.15 - 2022-12-31
- changed project layout.
  src/withings_client.clj => bb/main.clj
  launch from withing-cache diretory by;
```
% bb -m main
```

## 0.6.14 - 2022-12-31
- defined init-db.
- reason? user 17 is an invalid user.
```
(get-meas-all "2022-12-01")
; clojure.lang.ExceptionInfo: babashka.curl: status 400 withings-cache /Users/hkim/clojure/withings-cache/src/withings_cache.clj:28:3
```

## 0.6.13 - 2022-12-31
- git rm --cache curl.sh, which included Bearer token.
- namespace に分けない方が利用（配布、呼び出し）しやすいか？
- from-unix-time, to-unix-time using `date` command.

## 0.5.11 - 2022-12-30
- defined withings-test.

## 0.5.1 - 2022-12-30
### Fix typo(bug)
- lastupdate for lastdate
### Changed
- refresh-all! calls fetch-users internally.

## 0.5.0 - 2022-12-30
### Refactor
- `fetch-*` to `get-*` according to WITHINGS terminology.

## 0.4.9 - 2022-12-30
- withings returns all data when do not provide both meastype and meastypes. This was not described in WITHINGS's document.

## 0.3.8 - 2022-12-19
- (filter :valid (fetch-users)) でフィルタしたユーザで、
  (init-db "2022-09-01") できた。
  wc.kohhoh.jp でやってみるか。

## 0.3.7 - 2022-12-19
- git@github.com:hkim0331/withings-cache.git

## 0.3.6 - 2022-12-19
- prep for github
- gitignore /db-dump/
- gitignore /install/

## 0.3.5 - 2022-12-19
- defined (save-weight! id "datetime")
- unix timestamp to datetime.
  mysql has a unix_time(int) function.

## 0.3.4 - 2022-12-19
### Changed
- meas table definition
```
CREATE TABLE meas (
  id INTEGER PRIMARY KEY AUTO_INCREMENT,
  user_id INTEGER,  -- index of `users` table
  type    INT,      -- value colum in `measures` table
  measure FLOAT,    -- :measures [{:value 94400, :type 1, :unit -3, :algo 0, :fm 5}]
  created TIMESTAMP -- from_unixtime(1667176189)
  );
```

## 0.3.3 - 2022-12-19
fetch meas
- checked git configurations
- curl-get
- curl-post
- refresh-all-pmap!
- fetch-weight
- fetch-meas
- fetch-meas-with
- can fetch

## 0.2.2 - 2022-12-19
- login, users, tokens

## 0.1.0 - 2022-12-19
- started melt repository
