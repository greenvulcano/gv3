#!/bin/sh


BASEDIR=$(dirname $0)
. ${BASEDIR}/../../bin/env_scripts.sh 

export HSQLDB_HOME=$GV_HOME/hsqldb
export CLASSPATH=$HSQLDB_HOME/lib/hsqldb.jar

$JAVA_HOME/bin/java $JAVA_OPTS -cp $CLASSPATH org.hsqldb.util.DatabaseManagerSwing $@
