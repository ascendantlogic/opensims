#!/bin/sh

## $CVSId: purge.sh,v 1.2 2006/06/10 19:56:38 mikee Exp $
## $Id: purge.sh 1 2008-01-10 18:37:05Z smoot $
## Mike ERWIN mikee@symbiot.com
## Symbiot 5600
# A wrapper to handle postgres deletions directly through psql


table=$1
tick=$2
date=`date '+%c'`

echo "$0: $1 $2"
id

log_file=/var/log/opensims/purge.log

output=`/usr/bin/psql -U postgres -d opensims -c "begin; insert into alert_archive select * from alert where tick < $tick ; delete from $table where tick < $tick ; commit work;" 2>&1`

echo "$date Purging items from $table < tick $tick $output" >> $log_file;

exit 0
# END
