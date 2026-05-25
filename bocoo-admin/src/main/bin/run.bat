@echo off
title bocoo admin
reg add HKCU\Console /v VirtualTerminalLevel /t REG_DWORD /d 1 /f
chcp 65001

echo Starting bocoo admin application...

if "%SA_TOKEN_JWT_SECRET%"=="" set "SA_TOKEN_JWT_SECRET=bocoo-dev-jwt-secret-change-before-prod-2026"
if "%CRYPTO_AES_KEY%"=="" set "CRYPTO_AES_KEY=DevCryptoKey2026"

rem Set JAVA_OPTS for better management
set JAVA_OPTS=-Dfile.encoding=UTF-8 ^
-Dstdout.encoding=UTF-8 ^
-Dstderr.encoding=UTF-8 ^
-Duser.timezone=Asia/Shanghai ^
-Xms1g ^
-Xmx1g ^
-XX:ReservedCodeCacheSize=512m

rem Start Java Application using JAVA_OPTS
java %JAVA_OPTS% -jar bocoo-admin.jar

echo Application started.
pause
