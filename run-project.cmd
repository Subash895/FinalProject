@echo off
setlocal

pushd "%~dp0"

if "%~1"=="" goto :menu

if /I "%~1"=="publish-dockerhub" goto :publishdockerhub
if /I "%~1"=="local" goto :local
if /I "%~1"=="docker" goto :docker
if /I "%~1"=="stop-docker" goto :stopdocker

goto :menu

:menu
cls
echo Smart City Runner
echo.
echo 1. Upload Docker image to Docker Hub
echo 2. Run locally with H2
echo 3. Run in Docker with MySQL
echo 4. Stop Docker services
echo 5. Git add, commit, and push
echo 6. Exit
echo.
set /p choice=Choose an option [1-6]: 

if "%choice%"=="1" goto :publishdockerhub
if "%choice%"=="2" goto :local
if "%choice%"=="3" goto :docker
if "%choice%"=="4" goto :stopdocker
if "%choice%"=="5" goto :gitflow
if "%choice%"=="6" goto :end

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
set "SPRING_PROFILES_ACTIVE=local"
set "APP_PORT=8080"
for /f "delims=" %%A in ('powershell -NoProfile -Command "if (Get-NetTCPConnection -LocalPort 8080 -State Listen -ErrorAction SilentlyContinue) { 'busy' } else { 'free' }"') do set "PORT_STATE=%%A"
if /I "%PORT_STATE%"=="busy" set "APP_PORT=8081"
echo Starting Smart City locally with the local Spring profile on http://localhost:%APP_PORT%
call mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=%APP_PORT%
goto :end

:docker
echo Starting Smart City in Docker on http://localhost:8080 with MySQL
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
  git push
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

:end
popd
exit /b 0
