@echo off
rem FILE PURPOSE: Compile gameplay plus regression tests and run GameTests.
setlocal
rem BLOCK: Keep all source/resource paths relative to this project.
cd /d "%~dp0"
rem BLOCK: Use the recursive build script and its separate compilation target.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" -Target test -Run
exit /b %errorlevel%
