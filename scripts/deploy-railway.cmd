@echo off
setlocal

set "PROJECT_NAME=%~1"
if "%PROJECT_NAME%"=="" set "PROJECT_NAME=smart-city-finalproject"

set "APP_SERVICE=smartcity-app"
set "DB_SERVICE=MySQL"

echo Checking Railway CLI login...
call npx.cmd @railway/cli whoami >nul 2>&1
if errorlevel 1 (
  echo Railway login required.
  echo Starting browserless login flow...
  call npx.cmd @railway/cli login --browserless
  if errorlevel 1 goto :fail
)

if not exist ".railway" (
  echo Creating Railway project "%PROJECT_NAME%"...
  call npx.cmd @railway/cli init --name "%PROJECT_NAME%"
  if errorlevel 1 goto :fail

  echo Creating MySQL service "%DB_SERVICE%"...
  call npx.cmd @railway/cli add --database mysql --service "%DB_SERVICE%"
  if errorlevel 1 goto :fail

  echo Creating app service "%APP_SERVICE%"...
  call npx.cmd @railway/cli add --service "%APP_SERVICE%"
  if errorlevel 1 goto :fail

  echo Linking current directory to "%APP_SERVICE%"...
  call npx.cmd @railway/cli service link "%APP_SERVICE%"
  if errorlevel 1 goto :fail

  echo Setting Spring Boot database variables...
  call npx.cmd @railway/cli variable set -s "%APP_SERVICE%" ^
    "SPRING_DATASOURCE_URL=jdbc:mysql://${{MySQL.MYSQLHOST}}:${{MySQL.MYSQLPORT}}/${{MySQL.MYSQLDATABASE}}" ^
    "SPRING_DATASOURCE_USERNAME=${{MySQL.MYSQLUSER}}" ^
    "SPRING_DATASOURCE_PASSWORD=${{MySQL.MYSQLPASSWORD}}" ^
    "SPRING_JPA_HIBERNATE_DDL_AUTO=update" ^
    "SPRING_JPA_SHOW_SQL=false"
  if errorlevel 1 goto :fail
)

echo Deploying app to Railway...
call npx.cmd @railway/cli up -s "%APP_SERVICE%" -d
if errorlevel 1 goto :fail

echo Generating public domain...
call npx.cmd @railway/cli domain -s "%APP_SERVICE%"
if errorlevel 1 goto :fail

echo.
echo Deployment submitted successfully.
echo If this is the first deploy, wait for Railway to finish building the service.
echo You can inspect status with:
echo   npx.cmd @railway/cli status
echo   npx.cmd @railway/cli service logs "%APP_SERVICE%"
goto :eof

:fail
echo.
echo Railway deployment failed.
exit /b 1
