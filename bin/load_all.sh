#!/bin/bash

if [ $# -eq 0 ]; then
    echo "missing process date(%Y/%m/%d)"
    exit 1
fi

cd `dirname $0`
process_date=$1

set -xe

# sync
aws s3 sync s3://5miles-ads/users/${process_date} s3data/${process_date} --delete
# remove old
find s3data/ -mindepth 3 -type d -ctime +10 | xargs rm -rf

gzip -d s3data/$process_date/*.gz

mkdir -p logs/${process_date//\//}
nohup ./_load_all.sh $process_date > logs/${process_date//\//}/_out.log 2>&1 &
