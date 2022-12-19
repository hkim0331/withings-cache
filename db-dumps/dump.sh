#!/bin/sh
mysqldump -u user -p withings -h 127.0.0.1 -P 3306 > withings-`date +%F`.sql

