@echo off
REM Gnuplot Java CLI launcher script for Windows

REM Get the directory where this script is located
set SCRIPT_DIR=%~dp0

REM Path to the executable JAR
set JAR_PATH=%SCRIPT_DIR%target\gnuplot-cli-1.0.0-SNAPSHOT-jar-with-dependencies.jar

REM Check if JAR exists
if not exist "%JAR_PATH%" (
    echo Error: Executable JAR not found at %JAR_PATH%
    echo Please build the project first: mvn package
    exit /b 1
)

REM Run the CLI with all arguments passed through
java -jar "%JAR_PATH%" %*
