#!/bin/bash
if [ $# -eq 0 ]; then
  echo "No arguments supplied. "
  echo 'Usage : Script <process_date> <file_name> <list_id> [<batch_size> <skip:ios|android>]'
  exit 1
fi

process_date=$1
date_dir=${process_date//\//}
file_name=$2
list_id=$3
if [ "$4" != "" ]; then
  batch_size="-b $4"
fi
if [ "$5" != "" ]; then
  skip="-s $5"
fi
# set -x
java -Dlog4j.configurationFile=./log4j2.xml -DdateDir=$date_dir -DuserListFile=$file_name -jar target/bulk-uploader-1.0.jar \
  -f /fivemiles/scripts/bulk-uploader/s3data/$process_date/$file_name -l $list_id $batch_size $skip
