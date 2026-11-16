@echo off
rem FILE PURPOSE: Compile gameplay and open the Agyu window.
setlocal
rem BLOCK: Keep all source/resource paths relative to this project.
cd /d "%~dp0"
rem BLOCK: Use the recursive build script and its separate compilation target.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" -Target game -Run
if errorlevel 1 pause
