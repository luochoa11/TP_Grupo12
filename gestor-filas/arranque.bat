@echo off
title SGF - Iter3 - Alta Disponibilidad
set ROOT=%~dp0

REM =====================================
REM Servicio Directorio
REM =====================================
start "Directorio" cmd /k "cd /d %ROOT%servicio-directorio && java -cp target\classes;..\common\target\classes com.sgf.MainServicioDirectorio"
timeout /t 2 /nobreak > nul

REM =====================================
REM Monitor
REM =====================================
start "Monitor" cmd /k "cd /d %ROOT%monitor && java -cp target\classes;..\common\target\classes com.sgf.MainMonitor"
timeout /t 2 /nobreak > nul

REM =====================================
REM Servidores Centrales
REM =====================================
start "Servidor A" cmd /k "cd /d %ROOT%servidor-central && java -cp target\classes;..\common\target\classes com.sgf.MainServidor 8000"
timeout /t 2 /nobreak > nul

start "Servidor B" cmd /k "cd /d %ROOT%servidor-central && java -cp target\classes;..\common\target\classes com.sgf.MainServidor 9000"
timeout /t 2 /nobreak > nul

REM =====================================
REM Panel Administrador
REM =====================================
start "Panel Administrador" cmd /k "cd /d %ROOT%panel-administrador && java -cp target\classes;..\common\target\classes com.sgf.MainAdministrador"
timeout /t 1 /nobreak > nul

REM =====================================
REM Operadores
REM =====================================
start "Operador 1" cmd /k "cd /d %ROOT%panel-operador && java -cp target\classes;..\common\target\classes com.sgf.MainOperador 1"
timeout /t 1 /nobreak > nul

start "Operador 2" cmd /k "cd /d %ROOT%panel-operador && java -cp target\classes;..\common\target\classes com.sgf.MainOperador 2"
timeout /t 1 /nobreak > nul

REM =====================================
REM Registros
REM =====================================
start "Registro 1" cmd /k "cd /d %ROOT%terminal-registro && java -cp target\classes;..\common\target\classes com.sgf.MainRegistro 1"
timeout /t 1 /nobreak > nul

start "Registro 2" cmd /k "cd /d %ROOT%terminal-registro && java -cp target\classes;..\common\target\classes com.sgf.MainRegistro 2"
timeout /t 1 /nobreak > nul

REM =====================================
REM Pantalla Anuncio
REM =====================================
start "Anuncio" cmd /k "cd /d %ROOT%pantalla-anuncio && java -cp target\classes;..\common\target\classes com.sgf.MainAnuncio"

echo.
echo Todo el sistema fue iniciado correctamente.
pause