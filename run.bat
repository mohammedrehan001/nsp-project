@echo off
title Launching Medical Laboratory Management System...
echo ========================================================
echo   Starting Medical Laboratory Management System
echo ========================================================
echo.

if not exist "out\Main.class" (
    echo [INFO] Compiling first...
    call compile.bat
)

set "JAVA_CMD=java"
where java >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_CMD=%JAVA_HOME%\bin\java.exe"
)
if "%JAVA_CMD%"=="java" (
    for /d %%D in ("%USERPROFILE%\.jdks\*") do if exist "%%D\bin\java.exe" set "JAVA_CMD=%%D\bin\java.exe"
)
if "%JAVA_CMD%"=="java" (
    for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do if exist "%%D\jbr\bin\java.exe" set "JAVA_CMD=%%D\jbr\bin\java.exe"
)
if "%JAVA_CMD%"=="java" (
    for /d %%D in ("C:\Program Files\Java\*") do if exist "%%D\bin\java.exe" set "JAVA_CMD=%%D\bin\java.exe"
)

"%JAVA_CMD%" -cp "out;lib/*;src;." Main %*

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Application exited with an error.
    if not "%1"=="--no-pause" pause
)

