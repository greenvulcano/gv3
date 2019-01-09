#!/bin/sh


BASEDIR=$(dirname $0)
. ${BASEDIR}/env_scripts.sh

$JBOSS_HOME/bin/shutdown.sh -s jnp://localhost:$JBOSS_JNP_PORT -u $1 -p $2 -- -S
