@echo off
title SecureBank - Java Backend
echo ========================================================
echo Starting SecureBank Spring Boot Backend (Port 8080)...
echo ========================================================
cd /d "%~dp0backend"
call mvn spring-boot:run
pause
