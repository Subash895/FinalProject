@echo off
setlocal

pushd "%~dp0\.."

where railway >nul 2>&1
if errorlevel 1 (
  echo Railway CLI is not installed.
  echo Install it first: npm install -g @railway/cli
  popd
  exit /b 1
)

echo Checking Railway login...
railway whoami >nul 2>&1
if errorlevel 1 (
  echo Railway login is required.
  railway login
  if errorlevel 1 (
    echo Railway login failed.
    popd
    exit /b 1
  )
)

echo.
echo Make sure your Railway project already has:
echo 1. an app service using this repo or local directory
echo 2. a MySQL service
echo 3. these variables configured on the app service:
echo    SPRING_DATASOURCE_URL
echo    SPRING_DATASOURCE_USERNAME
echo    SPRING_DATASOURCE_PASSWORD
echo    SPRING_JPA_HIBERNATE_DDL_AUTO=update
echo.
echo Deploying current project to Railway...
railway up

set "EXIT_CODE=%ERRORLEVEL%"
popd
exit /b %EXIT_CODE%
