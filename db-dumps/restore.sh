#!/bin/sh
if [ -z "$1" ]; then
    echo usage: $0 yyyy-mm-dd.dumo
    exit 1
fi
DB=withings
CMD="mysql -u user -p -h 127.0.0.1"
#${CMD} -c "drop database ${DB}"
#${CMD} -c "create database ${DB}"
${CMD} withings < $1

