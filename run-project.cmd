@echo off
setlocal

pushd "%~dp0"

if exist ".env" (
  for /f "usebackq tokens=1,* delims==" %%A in (".env") do (
    if not "%%A"=="" if /I not "%%A"=="REM" set "%%A=%%B"
  )
)

if "%~1"=="" goto :menu

if /I "%~1"=="publish-dockerhub" goto :publishdockerhub
if /I "%~1"=="docker" goto :docker
if /I "%~1"=="stop-docker" goto :stopdocker
if /I "%~1"=="railway" goto :deployrailway

goto :menu

:menu
cls
echo Smart City Runner
echo.
echo 1. Upload Docker image to Docker Hub
echo 2. Run locally with MySQL
echo 3. Run in Docker with MySQL
echo 4. Stop Docker services
echo 5. Git add, commit, and push
echo 6. Deploy to Railway
echo 7. Exit
echo.
set /p choice=Choose an option [1-7]: 

if "%choice%"=="1" goto :publishdockerhub
if "%choice%"=="2" goto :local
if "%choice%"=="3" goto :docker
if "%choice%"=="4" goto :stopdocker
if "%choice%"=="5" goto :gitflow
if "%choice%"=="6" goto :deployrailway
if "%choice%"=="7" goto :end

echo.
echo Invalid choice.
pause
goto :menu

:publishdockerhub
set "DOCKER_REPO=smart-city-app"
set "DOCKER_TAG=latest"

echo Docker Hub publish
echo.
set /p DOCKER_USERNAME=Enter Docker Hub username: 
if "%DOCKER_USERNAME%"=="" goto :publishcancel

set /p INPUT_REPO=Enter repository name [smart-city-app]: 
if not "%INPUT_REPO%"=="" set "DOCKER_REPO=%INPUT_REPO%"

set /p INPUT_TAG=Enter tag [latest]: 
if not "%INPUT_TAG%"=="" set "DOCKER_TAG=%INPUT_TAG%"

set "FULL_IMAGE=%DOCKER_USERNAME%/%DOCKER_REPO%:%DOCKER_TAG%"

echo.
echo Logging in to Docker Hub...
docker login
if errorlevel 1 goto :publishfailed

echo.
echo Building image %FULL_IMAGE% ...
docker build -t %FULL_IMAGE% .
if errorlevel 1 goto :publishfailed

echo.
echo Pushing image %FULL_IMAGE% ...
docker push %FULL_IMAGE%
if errorlevel 1 goto :publishfailed

echo.
echo Docker image uploaded successfully:
echo %FULL_IMAGE%
pause
goto :end

:publishcancel
echo Docker Hub username is required.
pause
goto :menu

:publishfailed
echo.
echo Docker Hub upload failed.
pause
goto :menu

:local
echo Starting Smart City locally on http://localhost:8080 using MySQL
if "%SPRING_DATASOURCE_URL%"=="" (
  echo.
  echo SPRING_DATASOURCE_URL is not set.
  echo Set database values in .env before starting the local app.
  echo.
  pause
  goto :menu
)
.\mvnw.cmd spring-boot:run
goto :end

:docker
echo Starting Smart City in Docker on http://localhost:8080 with MySQL
if "%APP_AUTH_GOOGLE_CLIENT_ID%"=="" (
  echo.
  echo APP_AUTH_GOOGLE_CLIENT_ID is not set.
  echo Google login and Google register will be hidden until it is configured.
  echo Set it in .env before starting Docker.
  echo.
)
docker compose up --build
goto :end

:stopdocker
echo Stopping Smart City Docker services
docker compose down
goto :end

:gitflow
echo.
set /p RUN_ADD=Run git add . ? [y/N]: 
if /I "%RUN_ADD%"=="Y" (
  git add .
  if errorlevel 1 goto :gitfailed
)

echo.
set /p RUN_COMMIT=Run git commit ? [y/N]: 
if /I "%RUN_COMMIT%"=="Y" (
  set /p COMMIT_MESSAGE=Enter commit message: 
  if "%COMMIT_MESSAGE%"=="" (
    echo Commit message is required.
    pause
    goto :menu
  )
  git commit -m "%COMMIT_MESSAGE%"
  if errorlevel 1 goto :gitfailed
)

echo.
set /p RUN_PUSH=Run git push ? [y/N]: 
if /I "%RUN_PUSH%"=="Y" (
  echo.
  echo Available local branches:
  git branch
  echo.
  set "PUSH_BRANCH=main"
  set /p INPUT_BRANCH=Enter branch to push [main]: 
  if not "%INPUT_BRANCH%"=="" set "PUSH_BRANCH=%INPUT_BRANCH%"
  git push origin %PUSH_BRANCH%
  if errorlevel 1 goto :gitfailed
)

echo.
echo Git flow finished.
pause
goto :end

:gitfailed
echo.
echo Git command failed.
pause
goto :menu

:deployrailway
echo.
if exist ".\tools\railway\railway.exe" (
  set "RAILWAY_CLI=.\tools\railway\railway.exe"
) else (
  set "RAILWAY_CLI=railway"
)

echo Checking Railway login...
%RAILWAY_CLI% whoami
if errorlevel 1 (
  echo Railway login is required.
  pause
  goto :menu
)

echo.
echo Deploying current project to Railway...
if "%APP_AUTH_GOOGLE_CLIENT_ID%"=="" (
  echo.
  echo APP_AUTH_GOOGLE_CLIENT_ID is not set in the current environment.
  echo Add APP_AUTH_GOOGLE_CLIENT_ID in Railway Variables so Google login appears after deploy.
  echo.
)
%RAILWAY_CLI% up -d
if errorlevel 1 goto :railwayfailed

echo.
echo Railway deploy command finished.
pause
goto :end

:railwayfailed
echo.
echo Railway deployment failed.
pause
goto :menu

:end
popd
exit /b 0
