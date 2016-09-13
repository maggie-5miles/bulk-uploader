#!/bin/bash

if [ $# -eq 0 ]; then
    echo "missing process date(%Y/%m/%d)"
    exit 1
fi

process_date=$1
aws s3 sync s3://5miles-ads/users/${process_date} s3data/${process_date} --delete
