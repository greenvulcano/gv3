#!/bin/sh

BASEDIR=$(dirname $0)
. ${BASEDIR}/../../bin/env_scripts.sh

export HSQLDB_HOME=$GV_HOME/hsqldb
export HSQTOOL=$HSQLDB_HOME/lib/sqltool.jar

$JAVA_HOME/bin/java -jar $HSQTOOL  --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/gvesb,user=SA,password= --sql="shutdown;"
$JAVA_HOME/bin/java -jar $HSQTOOL  --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/ode,user=SA,password= --sql="shutdown;"
