#!/bin/bash
#
# edex_camel  This shell script takes care of starting and stopping
#                the AWIPS EDEX Camel instance.
#
# chkconfig: - 99 10
# description: Camel ESB System, which is the instance \
#              used by AWIPS EDEX.
# processname: start.sh
# config: /awips/edex/esb/conf/global.xml

# Source function library.
. /etc/rc.d/init.d/functions

# Source networking configuration.
. /etc/sysconfig/network

# Check that networking is up.
[ ${NETWORKING} = "no" ] && exit 0

RETVAL=0
prog="start.sh"

MEM=( `free -g | grep "Mem:"` )
TOTAL_MEM=${MEM[1]}

HIGH_MEM=off
if [ $TOTAL_MEM -gt 4 ]; then
   HIGH_MEM=on
fi

# determine services to load
SERVICES=( 'request' 'ingest' 'ingestGrib')

# Who to run EDEX server as, usually "awips".  (NOT "root")
EDEXUSER=awips

# Todays date in format of YYYYMMDD.
TODAY=`/bin/date +%Y%m%d`

# We will no longer be using hard-coded paths that need to be replaced.
# Use rpm to find the paths that we need.
JAVA_INSTALL="/awips2/java"
PYTHON_INSTALL="/awips2/python"
EDEX_INSTALL="/awips2/edex"

# The path that is to be used for the script
export JAVA_HOME=${JAVA_INSTALL}
export PATH=${JAVA_INSTALL}/bin:${PYTHON_INSTALL}/bin:/usr/local/sbin:/usr/local/bin:/sbin:/bin:/usr/sbin:/usr/bin
export LD_LIBRARY_PATH=${JAVA_INSTALL}/lib:${PYTHON_INSTALL}/lib
export LD_PRELOAD=${PYTHON_INSTALL}/lib/libpython2.7.so 
export AMQP_SPEC=""
export DATA_ARCHIVE_ROOT=/tmp/sbn

# what to do to start an EDEX instance
# $1 == instance token
startEDEX() {
   pidfile=${EDEX_INSTALL}/bin/${1}.pid
   CAMELPROCESS=`ps -ef | grep "edex.dev.mode"|grep -c "edex.run.mode=${1} " `
   if [ $CAMELPROCESS -eq 1 ]; then
      echo "WARNING: EDEX ${1} instance already running, not starting another instance"
      return 1
   fi

   EXTRA_ARGS="-noConsole"
   if [ $HIGH_MEM == "on" ]; then
      EXTRA_ARGS="${EXTRA_ARGS} -h"
   fi

   DAEMON="${EDEX_INSTALL}/bin/start.sh ${EXTRA_ARGS} ${1}"
   EDEXSTARTLOG=${EDEX_INSTALL}/logs/start-edex-${1}-$TODAY.log
   su $EDEXUSER -c "$DAEMON &" >> $EDEXSTARTLOG 2>&1
   sleep 5
   pid=`cat ${pidfile}`
   if [ "$pid" == "" ]; then
      echo "WARNING: No Wrapper Pid Found, EDEX ${1} did not start properly"
   fi
}

# what to do to stop an EDEX instance
# $1 == instance token
stopEDEX() {
   pidfile=${EDEX_INSTALL}/bin/${1}.pid
   if [ ! -f $pidfile ]; then
      echo "WARNING: EDEX ${1} instance not running, no shutdown attempted"
      return 1
   fi

   pidid=`cat ${pidfile}`
   kill $pidid
   savepid=$pidid
   CNT=0
   TOTCNT=0
   while [ "X$pidid" != "X" ]; do
      if [ "$CNT" -lt "3" ]; then
         let CNT=${CNT}+1
      else
         CNT=0
      fi
      let TOTCNT=${TOTCNT}+1
      sleep 1
      CAMELPROCESS=`ps -p $savepid -o args | grep home=${EDEX_INSTALL}/bin|grep -c "edex.run.mode=${1}"`
      if [ $CAMELPROCESS -eq 1 ]; then
         pidid=$savepid
      else
         pidid=""
      fi
   done
}

# what to use to check status
# $1 == instance token
checkStatus() {
        if [ -f ${EDEX_INSTALL}/bin/${1}.pid ]; then
                pidid=`cat ${EDEX_INSTALL}/bin/${1}.pid`
                CAMELPROCESS=`ps --ppid $pidid -o args | grep -c "edex.run.mode=${1}"`
                if [ $CAMELPROCESS -eq 1 ]; then
                        JAVAPROCESS=`ps --ppid $pidid -o pid,args  | grep "edex.run.mode=${1}"`
                        JAVAPROCESS=`echo $JAVAPROCESS | cut -d ' ' -f 1`
                        echo "EDEX Camel (${1}) is running (wrapper PID $pidid)"
                        echo "EDEX Camel (${1}) is running (java PID $JAVAPROCESS)"
                else
                        echo "EDEX Camel (${1}) is not running"
                fi
        else
                echo "EDEX Camel (${1}) is not running"
        fi
}

# Verify root user
checkUser() {
   REQUIREDUSER="root"
   CURUSER=`whoami`
   if [ "$CURUSER" != "$REQUIREDUSER" ]; then
      echo "Insufficient privileges: must run script as $REQUIREDUSER"
      exit 1
   fi
}

func=$1
shift 1
if [ $# -gt 0 ]; then
    SERVICES=("$@") 
fi

# See how we were called.
case $func in
   start)
      checkUser
      for service in ${SERVICES[*]};
      do
         echo -n "Starting EDEX Camel ($service): "
         startEDEX $service
         echo OK
      done
      RETVAL=$?
      ;;
   stop)
      checkUser
      for service in ${SERVICES[*]};
      do
         echo -n "Stopping EDEX Camel ($service): "
         stopEDEX $service
         echo OK
      done
      RETVAL=$?
      ;;
   restart)
      checkUser
      for service in ${SERVICES[*]};
      do
         echo -n "Stopping EDEX Camel ($service): "
         stopEDEX $service
         echo OK
      done
      sleep 5
      for service in ${SERVICES[*]};
      do
         echo -n "Starting EDEX Camel ($service): "
         startEDEX $service
         echo OK
      done
      RETVAL=$?
      ;;
   status)
      for service in ${SERVICES[*]};
      do
        checkStatus $service
      done
      ;;
   *)
      # Print help
      echo "Usage: $0 {start|stop|restart|reload|status} {service} {service}..." 1>&2
      echo "If service(s) blank it will start the default services of ${SERVICES[*]}" 1>&2
      exit 1
      ;;
esac

exit $RETVAL
