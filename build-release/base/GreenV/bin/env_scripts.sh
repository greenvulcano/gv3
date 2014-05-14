#!/bin/sh

export JBOSS_HOME=/home/gvesb/@base.dir@/jboss-5.1.0.GA
export GV_SERVER_NAME=GreenV
export GV_SERVER=$JBOSS_HOME/server/$GV_SERVER_NAME
export GV_HOME=/home/gvesb/@base.dir@/GreenV
export CLASSPATH=$GV_HOME/xmlconfig
export JAVA_HOME=/opt/platform/jdk1.6.0
export JBOSS_BIND_ADDRESS=0.0.0.0
export JBOSS_PORTSET=ports-default
export JBOSS_JNP_PORT=1099
