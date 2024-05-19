#!/bin/sh
#
# origin: ${utils}/src/bump-version.sh
#
# bump-vesion script for clojure projects.
# confused using macos's /usr/bin/sed. so gsed.
#
# CAUSION
# The POSIX standard regular expressions does not support back-references.
# Back-references are considered as an "extended" faciliy.
# This script, bump-version.sh, uses the extended function.
# So, gnu-sed on macOS.

if [ -z "$1" ]; then
    echo "usage: $0 <version>"
    exit
fi

# using extended regular expressions in this script, so,
if [ -x "${HOMEBREW_PREFIX}/bin/gsed" ]; then
    SED="${HOMEBREW_PREFIX}/bin/gsed -E"
else
    SED="/usr/bin/sed -E"
fi

# CHANGELOG.md
VER=$1
TODAY=`date +%F`
${SED} -i -e "/SNAPSHOT/c\
## ${VER} / ${TODAY}" CHANGELOG.md

# bb
${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" bb/main.clj

# project.clj
#${SED} -i "s/(defproject \S+) \S+/\1 \"$1\"/" project.clj

# cljs
#${SED} -i "s/(def \^:private version) .+/\1 \"$1\")/" src/main.cljs
