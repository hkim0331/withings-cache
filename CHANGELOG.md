# withings-cache

## Unreleased
- task
- meas
- when doing in kyutech, it takes longer time rather than in home. why?
- should be unique against same id and same created time. how?
- hyperfiddle.rcf

## 0.4.9-SNAPSHOT

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
