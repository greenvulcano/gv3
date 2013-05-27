@echo off

FOR /F "tokens=*" %%A IN ('cd') DO SET SOURCEDIR="%%A"
FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR="%%B"
cd "%BASEDIR%"

call ..\..\bin\env_scripts.cmd

set HSQLDB_HOME=%GV_HOME%\hsqldb
set CLASSPATH=%HSQLDB_HOME%\lib\hsqldb.jar

set JAVA_OPTS=-Xms256M -Xmx512M -XX:MaxPermSize=64m -XX:+UseConcMarkSweepGC

%JAVA_HOME%\bin\java %JAVA_OPTS% -cp %CLASSPATH% -DHSQLDB_HOME=%HSQLDB_HOME% org.hsqldb.server.Server

cd "%SOURCEDIR%"
