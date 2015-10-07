#!/bin/bash

mta_pre=https://datamine-history.s3.amazonaws.com/gtfs-
mta_pos=.tgz
dfs_pre=/mta/gtfs/hist/
dfs_pos=/gtfs.tgz

dget=$(date -d "-1 day" +%F)       # Dash  Yesterday
dput=$(date -d "-1 day" +%Y/%m/%d) # Slash Yesterday
hdfs dfs -mkdir -p ${dfs_pre}${dput}
curl ${mta_pre}${dget}${mta_pos} | hdfs dfs -put - ${dfs_pre}${dput}${dfs_pos}
