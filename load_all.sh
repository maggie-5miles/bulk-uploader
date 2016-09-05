#!/bin/bash

if [ $# -eq 0 ]; then
    echo "missing process date(%Y/%m/%d)"
    exit 1
fi

cd `dirname $0`
./s3sync.sh

process_date=$1
gzip -d s3data/$process_date/*.gz

mkdir -p logs/${process_date//\//}
nohup ./_load_all.sh $process_date > logs/${process_date//\//}/_out.log 2>&1 &
