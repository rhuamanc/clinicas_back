@echo off
setlocal

REM Fuerza JDK 21 solo para esta ejecucion
set "JAVA_HOME=C:\Program Files\Microsoft\jdk-21.0.1.12-hotspot"
if not exist "%JAVA_HOME%\bin\java.exe" (
  echo No se encontro JDK 21 en "%JAVA_HOME%"
  exit /b 1
)

set "PATH=%JAVA_HOME%\bin;%PATH%"

mvn spring-boot:run
