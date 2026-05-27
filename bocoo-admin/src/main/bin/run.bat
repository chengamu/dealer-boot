@echo off
title bocoo admin
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f
chcp 65001

echo Starting bocoo admin application...

%~d0
cd %~dp0

if "%SA_TOKEN_JWT_SECRET%"=="" set "SA_TOKEN_JWT_SECRET=bocoo-dev-jwt-secret-change-before-prod-2026"
if "%CRYPTO_AES_KEY%"=="" set "CRYPTO_AES_KEY=DevCryptoKey2026"
if "%SPRING_PROFILES_ACTIVE%"=="" set "SPRING_PROFILES_ACTIVE=dev"

set "APP_HOME=%~dp0"
set "CONFIG_DIR=%APP_HOME%config\"

rem Set JAVA_OPTS for better management
set JAVA_OPTS=-Dfile.encoding=UTF-8 ^
-Dstdout.encoding=UTF-8 ^
-Dstderr.encoding=UTF-8 ^
-Duser.timezone=UTC ^
-Xms1g ^
-Xmx1g ^
-XX:ReservedCodeCacheSize=512m

rem Start Java Application from JAR and load external DEV config from ./config/application-dev.yml
java %JAVA_OPTS% -jar "%APP_HOME%bocoo-admin.jar" --spring.profiles.active=%SPRING_PROFILES_ACTIVE% "--spring.config.additional-location=file:%CONFIG_DIR%"

echo Application started.
pause
