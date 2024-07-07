@echo off
REM Check if script is running with administrative privileges
NET SESSION >nul 2>&1
IF %ERRORLEVEL% NEQ 0 (
    ECHO Please run the script as an Administrator
    EXIT /B
)

REM Move the javafx-sdk-21.0.2 folder into C:\Program Files\Java\
move /Y "javafx-sdk-21.0.2" "C:\Program Files\Java\"

REM Open jdk-21_windows-x64_bin.exe
start "" "jdk-21_windows-x64_bin.exe"