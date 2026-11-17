@echo off
title Compiling Medical Laboratory Management System...
echo ========================================================
echo     Compiling Medical Laboratory Management System
echo ========================================================
echo.

if not exist "out" mkdir "out"

set "JAVAC_CMD=javac"
where javac >nul 2>nul
if %ERRORLEVEL% NEQ 0 (
    if defined JAVA_HOME if exist "%JAVA_HOME%\bin\javac.exe" set "JAVAC_CMD=%JAVA_HOME%\bin\javac.exe"
)
if "%JAVAC_CMD%"=="javac" (
    for /d %%D in ("%USERPROFILE%\.jdks\*") do if exist "%%D\bin\javac.exe" set "JAVAC_CMD=%%D\bin\javac.exe"
)
if "%JAVAC_CMD%"=="javac" (
    for /d %%D in ("C:\Program Files\JetBrains\IntelliJ IDEA*") do if exist "%%D\jbr\bin\javac.exe" set "JAVAC_CMD=%%D\jbr\bin\javac.exe"
)
if "%JAVAC_CMD%"=="javac" (
    for /d %%D in ("C:\Program Files\Java\*") do if exist "%%D\bin\javac.exe" set "JAVAC_CMD=%%D\bin\javac.exe"
)

echo [INFO] Using Java Compiler: "%JAVAC_CMD%"
"%JAVAC_CMD%" -encoding UTF-8 -cp "lib/*;." -d "out" src\*.java

if %ERRORLEVEL% NEQ 0 (
    echo.
    echo [ERROR] Compilation failed!
    if not "%1"=="--no-pause" pause
    exit /b %ERRORLEVEL%
)

echo.
echo ========================================================
echo    [SUCCESS] Compilation completed successfully!
echo    You can now launch the application using: run.bat
echo ========================================================
if not "%1"=="--no-pause" pause

