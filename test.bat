@echo off
setlocal
cd /d "%~dp0"
if not exist build mkdir build
javac -encoding UTF-8 -Xlint:all -d build *.java
if errorlevel 1 exit /b 1
java -Djava.awt.headless=true -cp "build;." GameTests
