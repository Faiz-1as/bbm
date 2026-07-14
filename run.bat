@echo off
setlocal enabledelayedexpansion

echo ========================================
echo        Bomberman - Build and Run
echo ========================================
echo.

REM Set JAVA_HOME if not already set
if "%JAVA_HOME%"=="" set "JAVA_HOME=C:\Program Files\BellSoft\LibericaJDK-21"

REM Use local Maven installation (absolute path)
set "MAVEN_HOME=%~dp0.maven\apache-maven-3.9.9"
set "MVN_CMD=%MAVEN_HOME%\bin\mvn.cmd"

if not exist "%MVN_CMD%" (
    echo [ERROR] Maven not found at: %MVN_CMD%
    echo Please ensure .maven\apache-maven-3.9.9 exists.
    pause
    exit /b 1
)

echo [1/2] Building with Maven...
call "%MVN_CMD%" clean compile -q
if !ERRORLEVEL! NEQ 0 (
    echo.
    echo Build FAILED! See errors above.
    pause
    exit /b 1
)

echo [2/2] Starting game...
echo.
call "%MVN_CMD%" javafx:run
pause
