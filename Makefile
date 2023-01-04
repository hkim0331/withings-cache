init:
	@echo call main/init-meas
# bb -x main/init-meas
# 2022-12-31T07:31:34.320Z m2.local DEBUG [main:?] - get-meas-with id=16&lastupdate={}
# ----- Error --------------------------------------------------------------------
# Type:     clojure.lang.ExceptionInfo
# Message:  babashka.curl: status 400
# Data:     {:status 400, :headers {"server" "nginx/1.18.0 (Ubuntu)", "date" "Sat, 31 Dec 2022 07:31:34 GMT", "content-type" "application/octet-stream", "content-length" "17", "connection" "keep-alive", "x-content-type-options" "nosniff", "x-frame-options" "SAMEORIGIN"}, :body "Conversion failed", :err "", :process #object[java.lang.ProcessImpl 0x3d161b9c "Process[pid=27466, exitValue=0]"], :exit 0}
# Location: /Users/hkim/clojure/withings-cache/bb/main.clj:44:3

update:
	bb -m main >> log/`date +%F`.log

mysql: mycli
mycli:
	mycli -u ${MYSQL_USER} -h 127.0.0.1 withings

service:
	cp systemd/withings-cache.service /lib/systemd/system
	cp systemd/withings-cache.timer   /lib/systemd/system
	systemctl daemon-reload
	systemctl enable withings-cache
	systemctl start  withings-cache.timer
	systemctl enable withings-cache.timer

stop: disable
disable:
	systemctl stop  withings-cache.timer
	systemctl disable withings-cache.timer
