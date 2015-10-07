#!/bin/bash

mta_pre=https://datamine-history.s3.amazonaws.com/gtfs-
mta_pos=.tgz
dfs_pre=/mta/gtfs/hist/
dfs_pos=/gtfs.tgz

start=2014-09-17 # The earliest historical date. I thought it went back 2-3 years... but no
end=$(date -d "-1 day" +%F) # Yesterday
while ! [[ $start > $end ]]; do
    dget="$start"
    dput="${start//-//}"
    hdfs dfs -mkdir -p ${dfs_pre}${dput}
    curl ${mta_pre}${dget}${mta_pos} | hdfs dfs -put - ${dfs_pre}${dput}${dfs_pos}

    start=$(date -d "$start + 1 day" +%F)
done
