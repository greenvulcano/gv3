@echo off

FOR /F "tokens=*" %%A IN ('cd') DO SET SOURCEDIR="%%A"
FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR="%%B"
cd "%BASEDIR%"

call ..\..\bin\env_scripts.cmd

set HSQLDB_HOME=%GV_HOME%\hsqldb
set CLASSPATH=%HSQLDB_HOME%\lib\hsqldb.jar

"%JAVA_HOME%\bin\java" -cp %CLASSPATH% org.hsqldb.util.DatabaseManagerSwing %1 %2 %3 %4 %5 %6 %7 %8 %9

cd "%SOURCEDIR%"
