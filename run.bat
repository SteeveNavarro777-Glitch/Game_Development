@echo off
setlocal
cd /d "%~dp0"
where javac >nul 2>nul
if errorlevel 1 (
    echo Java JDK 17 or newer is required. Add its bin folder to PATH.
    pause
    exit /b 1
)
if not exist build mkdir build
javac -encoding UTF-8 -Xlint:all -d build *.java
if errorlevel 1 (
    pause
    exit /b 1
)
java -cp "build;." Main
if errorlevel 1 pause
