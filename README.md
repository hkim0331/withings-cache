# withings-cache

Withings から取ってきたデータを MariaDB にストアする babashka スクリプト。
kohhoh でも、ローカル PC でも同じことができるように。

## Required

* babashka

    % brew install babashka
    % bb --version
    babashka v1.0.168

* mariadb (or mysql-server)

    % brew install mariadb
    $ apt install mariadb-server
    mysql 10.3.37-MariaDB-0ubuntu0.20.04.1


## Usage

初期化はどうする？

毎日のアップデートは、

    % make update

or

    % bb -m main

これを systemd から呼び出す。

