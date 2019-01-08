@echo off

FOR /F "tokens=*" %%B IN ('echo %~dp0') DO SET BASEDIR=%%B
call "%BASEDIR%"\env_scripts.cmd

"%JBOSS_HOME%\bin\shutdown.bat" -s jnp://localhost:%JBOSS_JNP_PORT% -u %1 -p %2 -- -S
