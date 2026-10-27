@echo off
setlocal
set GRADLE_VERSION=8.9
if "%GRADLE_USER_HOME%"=="" set GRADLE_USER_HOME=%USERPROFILE%\.gradle
set CACHE_ROOT=%GRADLE_USER_HOME%\wrapper\dists\gradle-%GRADLE_VERSION%-bin\recipebook
set GRADLE_EXE=%CACHE_ROOT%\gradle-%GRADLE_VERSION%\bin\gradle.bat
if not exist "%GRADLE_EXE%" (
  if not exist "%CACHE_ROOT%" mkdir "%CACHE_ROOT%"
  echo Gradle %GRADLE_VERSION% not found; downloading distribution...
  powershell -NoProfile -ExecutionPolicy Bypass -Command "Invoke-WebRequest -Uri 'https://services.gradle.org/distributions/gradle-%GRADLE_VERSION%-bin.zip' -OutFile '%CACHE_ROOT%\gradle.zip'; Expand-Archive -Path '%CACHE_ROOT%\gradle.zip' -DestinationPath '%CACHE_ROOT%' -Force; Remove-Item '%CACHE_ROOT%\gradle.zip'"
  if errorlevel 1 exit /b 1
)
call "%GRADLE_EXE%" %*
endlocal
