@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"

if exist "%WRAPPER_JAR%" goto run

echo Maven Wrapper jar not found, downloading...
set "JAVA_EXE=java"
if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" -cp "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" MavenWrapperDownloader "%MAVEN_PROJECTBASEDIR%"
if errorlevel 1 goto fail

:run
set "JAVA_EXE=java"
if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -cp "%WRAPPER_JAR%" ^
  %WRAPPER_LAUNCHER% %*

if errorlevel 1 goto fail
goto end

:fail
echo Failed to run Maven Wrapper.
exit /b 1

:end
endlocal
@REM ----------------------------------------------------------------------------
@REM Maven Wrapper startup batch script, Windows
@REM ----------------------------------------------------------------------------
@echo off
setlocal

set "MAVEN_PROJECTBASEDIR=%~dp0"
if "%MAVEN_PROJECTBASEDIR:~-1%"=="\" set "MAVEN_PROJECTBASEDIR=%MAVEN_PROJECTBASEDIR:~0,-1%"

set "WRAPPER_JAR=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.jar"
set "WRAPPER_LAUNCHER=org.apache.maven.wrapper.MavenWrapperMain"
set "WRAPPER_PROPERTIES=%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties"

if exist "%WRAPPER_JAR%" goto run

echo Maven Wrapper jar not found, downloading...
if not exist "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" mkdir "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper"

set "JAVA_EXE=java"
if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" -cp "%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" MavenWrapperDownloader "%MAVEN_PROJECTBASEDIR%"
if errorlevel 1 goto fail

:run
set "JAVA_EXE=java"
if not "%JAVA_HOME%"=="" if exist "%JAVA_HOME%\bin\java.exe" set "JAVA_EXE=%JAVA_HOME%\bin\java.exe"

"%JAVA_EXE%" ^
  -Dmaven.multiModuleProjectDirectory="%MAVEN_PROJECTBASEDIR%" ^
  -Dmaven.home="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper" ^
  -Dclassworlds.conf="%MAVEN_PROJECTBASEDIR%\.mvn\wrapper\maven-wrapper.properties" ^
  -cp "%WRAPPER_JAR%" ^
  %WRAPPER_LAUNCHER% %*

if errorlevel 1 goto fail
goto end

:fail
echo Failed to run Maven Wrapper.
exit /b 1

:end
endlocal
