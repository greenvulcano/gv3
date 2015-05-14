#!/bin/sh

BASEDIR=$(dirname $0)
. ${BASEDIR}/env_scripts.sh

export JB_OPTS="-Djboss.service.binding.set=$JBOSS_PORTSET -Djboss.partition.name=$GV_SERVER_NAMEPartition -Djboss.bind.address=$JBOSS_BIND_ADDRESS -Dbind.address=localhost -Djava.net.preferIPv4Stack=true -Djava.rmi.server.hostname=localhost -Dorg.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER=true"
export GV_OPTS="-Dgv.app.home=$GV_HOME -Dit.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath=GVCore.xml|/GVCore/GVXPath/XPath -Djavax.xml.parsers.DocumentBuilderFactory=org.apache.xerces.jaxp.DocumentBuilderFactoryImpl -Djavax.xml.transform.TransformerFactory=org.apache.xalan.processor.TransformerFactoryImpl -Djxl.nogc=true -Dit.greenvulcano.log.db.JDBCAppender.logState=false -Dgv.pop.uidcache.manager=it.greenvulcano.gvesb.virtual.pop.uidcache.MemoryUIDCacheManager -Dit.greenvulcano.util.thread.BaseThread.dumpInstCount=true -Dit.greenvulcano.util.thread.BaseThread.dumpCreateStack=false -Dit.greenvulcano.gvesb.identity.IdentityInfo.debug=true -Dorg.jruby.embed.localcontext.scope=threadsafe -Dpython.path=$GV_HOME/scripts/jython"
export JAVA_OPTS="-Xms512M -Xmx1024M -XX:MaxPermSize=256m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled"

cp $GV_SERVER/log/server.log $GV_SERVER/log/server.log.`date +%Y%m%d%H%M%S` >> /dev/null 2>&1
rm -rf $GV_SERVER/work
rm -rf $GV_SERVER/tmp

$JBOSS_HOME/bin/run.sh -c $GV_SERVER_NAME -C $CLASSPATH $JB_OPTS $GV_OPTS
