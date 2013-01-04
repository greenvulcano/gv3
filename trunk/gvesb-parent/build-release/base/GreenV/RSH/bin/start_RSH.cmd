@echo off

FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR=%%B
call "%BASEDIR%"\..\bin\env_scripts.cmd

set RSH_HOME=%GV_HOME%\RSH
set CLASSPATH=%RSH_HOME%\conf;%RSH_HOME%\lib\gvbase.jar;%RSH_HOME%\lib\gvrsh_commons.jar;%RSH_HOME%\lib\gvrsh_server.jar;%RSH_HOME%\lib\commons-logging.jar;%RSH_HOME%\lib\commons-io.jar;%RSH_HOME%\lib\jaxen.jar;%RSH_HOME%\lib\log4j.jar;%RSH_HOME%\lib\rmiio.jar

set RSH_OPTS=-Drsh.app.home=%RSH_HOME%
set JAVA_OPTS=-server -Xms128M -Xmx256M -XX:MaxPermSize=64m -XX:+UseConcMarkSweepGC -XX:+CMSClassUnloadingEnabled

%JAVA_HOME%\bin\java %JAVA_OPTS% -cp %CLASSPATH% %RSH_OPTS% it.greenvulcano.gvesb.rsh.server.RSHServer -p 3099


