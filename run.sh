#!/bin/bash
if [ $# -eq 0 ]; then
  echo "No arguments supplied. "
  echo 'Usage : Script <file_name> <list_id> [<batch_size> <skip:ios|android>]'
  exit 1
fi

file_name=$1
list_id=$2
if [ "$3" != "" ]; then
  batch_size="-b $3"
fi
if [ "$4" != "" ]; then
  skip="-s $4"
fi
echo "java -Dlog4j.configurationFile=./log4j2.xml -DuserListFile=$file_name -jar bulk-uploader-1.0-jar-with-dependencies.jar -f $file_name -l $list_id $batch_size $skip"
