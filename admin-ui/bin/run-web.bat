@echo off
echo.
echo [Info] Starting admin-ui dev server with Vite.
echo.

%~d0
cd %~dp0

cd ..
pnpm dev

pause
