#!/bin/sh
# can not. Invalid Params
#curl --header "Authorization: Bearer 3c4bf457c300f827755f7b488a5d26cf14761ecd" --data "action=getmeas&meastype=999,999&meastypes=&category=1&startdate=&enddate=&offset=&lastupdate=1669849930" 'https://wbsapi.withings.net/measure'


# "measures":[
#     {"value":53800003,"type":1,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":42400003,"type":5,"unit":-6,"algo":0,"fm":131},
#     {"value":21189589,"type":6,"unit":-6,"algo":0,"fm":131},
#     {"value":11399999,"type":8,"unit":-6,"algo":0,"fm":131},
#     {"value":40200000,"type":76,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":29599998,"type":77,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":2200000,"type":88,"unit":-6,"algo":0,"fm":131,"apppfmid":9}]

#curl --header "Authorization: Bearer 3c4bf457c300f827755f7b488a5d26cf14761ecd" --data "action=getmeas&category=1&startdate=&enddate=&offset=&lastupdate=1669849930" 'https://wbsapi.withings.net/measure'

# "measures":[
#     {"value":53800003,"type":1,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":42400003,"type":5,"unit":-6,"algo":0,"fm":131},
#     {"value":21189589,"type":6,"unit":-6,"algo":0,"fm":131},
#     {"value":11399999,"type":8,"unit":-6,"algo":0,"fm":131},
#     {"value":40200000,"type":76,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":29599998,"type":77,"unit":-6,"algo":0,"fm":131,"apppfmid":9},
#     {"value":2200000,"type":88,"unit":-6,"algo":0,"fm":131,"apppfmid":9}]

# use 近藤's token. must without quotation marks.
token=61e22f2ab568f304b4791e498484291479d4b085
curl \
  --header "Authorization: Bearer ${token}" \
  --data "action=getmeas&lastupdate=1669849930" \
  'https://wbsapi.withings.net/measure' | jq
