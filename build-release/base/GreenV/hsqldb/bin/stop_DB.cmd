@echo off

FOR /F "tokens=*" %%A IN ('cd') DO SET SOURCEDIR="%%A"
FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR="%%B"
cd "%BASEDIR%"

call ..\..\bin\env_scripts.cmd

set HSQLDB_HOME=%GV_HOME%\hsqldb
set HSQTOOL=%HSQLDB_HOME%\lib\sqltool.jar

%JAVA_HOME%\bin\java -jar %HSQTOOL%  --inlineRc=url=jdbc:hsqldb:hsql://localhost:9001/gvesb,user=SA,password= --sql="shutdown;"

cd "%SOURCEDIR%"
