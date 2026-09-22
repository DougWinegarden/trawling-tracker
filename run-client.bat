@echo off
rem Starts a development RuneLite client with Trawling Tracker loaded.
rem Jagex account? Do the one-time setup in README.md first.
cd /d "%~dp0"
call "%~dp0gradlew.bat" run
if errorlevel 1 pause
