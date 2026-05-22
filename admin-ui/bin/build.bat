@echo off
echo.
echo [Info] Building admin-ui into dist.
echo.

%~d0
cd %~dp0

cd ..
npm run build

pause
