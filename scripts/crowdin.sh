#!/bin/zsh

basedir=$(git rev-parse --show-toplevel)
apikey=$(tr -d ' \r\n' < "$basedir/scripts/crowdin.key")
location='app/src/main/res'

if [ -z "$basedir" -o -z "$apikey" ]; then
    echo 'API key missing'
    exit 1
fi

if [ -n "$(git status --porcelain $basedir/$location)" ]; then
    echo 'Outstanding changes:'
    git status --short "$basedir/$location"
    exit 1
fi

response=$(curl -sS "https://api.crowdin.com/api/project/leafpic/export?key=$apikey" | grep '<success' )
echo $response

if [ -n "$response" ]; then
    tempfile=$(mktemp)
    wget -qO "$tempfile" "https://api.crowdin.com/api/project/leafpic/download/all.zip?key=$apikey"
    unzip -oqd "$basedir/$location" "$tempfile"
    rm "$tempfile"
    git --no-pager diff --stat --no-ext-diff "$basedir/$location"
fi