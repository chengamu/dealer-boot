@echo off
echo.
echo [Info] Building admin-ui into dist.
echo.

%~d0
cd %~dp0

cd ..
pnpm build

pause
