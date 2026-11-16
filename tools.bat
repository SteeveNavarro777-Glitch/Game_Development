@echo off
rem FILE PURPOSE: Compile the current developer tools and run the selected tool.
setlocal
rem BLOCK: Keep all source/resource paths relative to this project.
cd /d "%~dp0"
if "%~1"=="" (
    echo Usage: tools.bat RpgVillagePreviews
    echo Importers: SpriteImporter, GuardSpriteImporter, CombatBodyImporter, RpgAssetImporter
    exit /b 1
)
rem BLOCK: Use the recursive build script and its separate compilation target.
powershell -NoProfile -ExecutionPolicy Bypass -File "%~dp0build.ps1" -Target tools -Run -EntryPoint "%~1"
exit /b %errorlevel%
