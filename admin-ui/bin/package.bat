@echo off
echo.
echo [Info] Installing admin-ui dependencies with pnpm.
echo.

%~d0
cd %~dp0

cd ..
pnpm install --registry=https://registry.npmmirror.com

pause
