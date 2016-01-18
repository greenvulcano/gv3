#!/bin/sh

BASEDIR=$(dirname $0)
. ${BASEDIR}/../../bin/env_scripts.sh

export HSQLDB_HOME=$GV_HOME/hsqldb
export CLASSPATH=$HSQLDB_HOME/lib/hsqldb.jar

export JAVA_OPTS="-Xms256M -Xmx512M -XX:MaxPermSize=64m -XX:+UseConcMarkSweepGC -DHSQLDB_HOME=$HSQLDB_HOME"

cd ${BASEDIR}
$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH org.hsqldb.server.Server
cd -
