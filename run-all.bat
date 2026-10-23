@echo off
title SecureBank - Project Launcher
echo ========================================================
echo Starting SecureBank Full Stack System...
echo ========================================================
echo Starting Backend (Port 8080) in new window...
start "SecureBank - Java Backend" cmd /k "cd /d ""%~dp0backend"" && mvn spring-boot:run"

echo Starting Frontend (Port 5173) in new window...
start "SecureBank - React Frontend" cmd /k "cd /d ""%~dp0frontend"" && npm run dev"

echo ========================================================
echo Both services are launching!
echo Once both windows load, open: http://localhost:5173/login
echo ========================================================
timeout /t 5 >nul
start http://localhost:5173/login
