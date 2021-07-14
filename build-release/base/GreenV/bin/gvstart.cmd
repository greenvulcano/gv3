@echo off

FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR=%%B
call "%BASEDIR%"\env_scripts.cmd

set JB_OPTS=-Djboss.service.binding.set=%JBOSS_PORTSET% -Djboss.partition.name="%GV_SERVER_NAME%Partition" -Djboss.bind.address="%JBOSS_BIND_ADDRESS%" -Dbind.address="localhost" -Djava.rmi.server.hostname="localhost" -Djava.net.preferIPv4Stack="true" -Djava.rmi.server.hostname="localhost" -Djxl.nogc=true -Dorg.apache.coyote.USE_CUSTOM_STATUS_MSG_IN_HEADER=true
set GV_OPTS=-Dgv.app.home=/%GV_HOME% -Dit.greenvulcano.util.xpath.search.XPathAPIFactory.cfgFileXPath="GVCore.xml|/GVCore/GVXPath/XPath" -Djavax.xml.parsers.DocumentBuilderFactory="org.apache.xerces.jaxp.DocumentBuilderFactoryImpl" -Djavax.xml.transform.TransformerFactory="org.apache.xalan.processor.TransformerFactoryImpl" -Dgv.pop.uidcache.manager="it.greenvulcano.gvesb.virtual.pop.uidcache.MemoryUIDCacheManager" -Dit.greenvulcano.util.thread.BaseThread.dumpInstCount=true -Dit.greenvulcano.util.thread.BaseThread.dumpCreateStack=false -Dit.greenvulcano.gvesb.identity.IdentityInfo.debug=true
set JAVA_OPTS=-Xms512M -Xmx1024M -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled


"%JBOSS_HOME%\bin\run.bat" -c %GV_SERVER_NAME% -C %CLASSPATH% %JB_OPTS% %GV_OPTS%
