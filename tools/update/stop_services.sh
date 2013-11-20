#!/bin/sh

## $CVSId: stop_services.sh,v 1.4 2005/01/10 15:56:33 mikee Exp $
## $Id: stop_services.sh 1 2008-01-10 18:37:05Z smoot $
## force shutdown of Tomcat webapp and local symagent
## Mike ERWIN mikee@symbiot.com
## Paco NATHAN paco@symbiot.com
## Jamie PUGH jamie@symbiot.com


nuke ()
{
    sudo /etc/init.d/$1 stop

    ps axl | grep $1 | grep -v grep | perl -e 'while (<STDIN>) { s/\s+/ /g; @l = split(/ /); print $l[2] . "\n"; }' |
    while read pid
    do
      printf "kill pid %6d  %s\n" $pid $1
      sudo kill -9 $pid
    done

    sudo /etc/init.d/$1 zap

    return 0
}


## main entry point

nuke symagent
/etc/init.d/snort stop
/etc/init.d/nagios stop
