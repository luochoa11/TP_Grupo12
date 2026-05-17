@echo off
title SGF - Iter3 - Alta Disponibilidad

set ROOT=%~dp0

REM =====================================
REM Servicio Directorio
REM =====================================
start "Directorio" cmd /k "cd /d %ROOT%servicio-directorio && java -cp target\classes;..\common\target\classes com.sgf.MainServicioDirectorio"

timeout /t 1 > nul

REM =====================================
REM Monitor
REM =====================================
start "Monitor" cmd /k "cd /d %ROOT%monitor && java -cp target\classes;..\common\target\classes com.sgf.MainMonitor"

timeout /t 1 > nul

REM =====================================
REM Servidor Primario
REM =====================================
start "Servidor Primario " cmd /k "cd /d %ROOT%servidor-central && java -cp target\classes;..\common\target\classes com.sgf.MainServidor" 8000
timeout /t 2 > nul
REM =====================================
REM Servidor Secundario  
REM =====================================
start "Servidor Secundario " cmd /k "cd /d %ROOT%servidor-central && java -cp target\classes;..\common\target\classes com.sgf.MainServidor" 9000

timeout /t 2 > nul

REM =====================================
REM Operador 1
REM =====================================
start "Operador 1" cmd /k "cd /d %ROOT%panel-operador && java -cp target\classes;..\common\target\classes com.sgf.MainOperador 1"

timeout /t 1 > nul

REM =====================================
REM Operador 2
REM =====================================
start "Operador 2" cmd /k "cd /d %ROOT%panel-operador && java -cp target\classes;..\common\target\classes com.sgf.MainOperador 2"

timeout /t 1 > nul

REM =====================================
REM Registro 1
REM =====================================
start "Registro 1" cmd /k "cd /d %ROOT%terminal-registro && java -cp target\classes;..\common\target\classes com.sgf.MainRegistro 1"

timeout /t 1 > nul

REM =====================================
REM Registro 2
REM =====================================
start "Registro 2" cmd /k "cd /d %ROOT%terminal-registro && java -cp target\classes;..\common\target\classes com.sgf.MainRegistro 2"

timeout /t 1 > nul

REM =====================================
REM Pantalla Anuncio
REM =====================================
start "Anuncio" cmd /k "cd /d %ROOT%pantalla-anuncio && java -cp target\classes;..\common\target\classes com.sgf.MainAnuncio"

echo.
echo Todo iniciado.
pause