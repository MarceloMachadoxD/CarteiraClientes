@echo off
setlocal enabledelayedexpansion

set DEFAULT_PORT=8080
set FALLBACK_PORT=9090
set PORT=%DEFAULT_PORT%

:: Verifica se algo esta rodando na porta 8080
netstat -ano | findstr ":%DEFAULT_PORT% " | findstr "LISTENING" >nul 2>&1
if errorlevel 1 goto :start

:: Obtem o PID do processo na porta 8080
for /f "tokens=5" %%p in ('netstat -ano ^| findstr ":%DEFAULT_PORT% " ^| findstr "LISTENING"') do set PID=%%p

:: Verifica se e o proprio projeto (procura pelo jar do projeto no comando do processo)
powershell -NoProfile -Command "Get-Process -Id %PID% -ErrorAction SilentlyContinue | Select-Object -ExpandProperty Path" 2>nul | findstr /i "java" >nul 2>&1
if not errorlevel 1 (
    :: E um processo Java — verifica se e o CarteiraClientes pelo modulo
    powershell -NoProfile -Command "(Get-Process -Id %PID% -ErrorAction SilentlyContinue).Modules.FileName -join ' '" 2>nul | findstr /i "CarteiraClientes" >nul 2>&1
    if not errorlevel 1 (
        echo [INFO] O projeto ja esta rodando na porta %DEFAULT_PORT% ^(PID %PID%^).
        echo [INFO] Encerrando processo existente para reiniciar...
        taskkill /PID %PID% /F >nul 2>&1
        timeout /t 2 /nobreak >nul
        goto :start
    )
)

:: Porta ocupada por outro processo — pergunta com timeout de 5s
echo.
echo [AVISO] A porta %DEFAULT_PORT% esta em uso por outro processo ^(PID %PID%^).
echo.
echo Deseja informar uma porta diferente? ^(S/N^)
echo Pressione S para digitar a porta, ou aguarde 5 segundos para usar a porta %FALLBACK_PORT%.
echo.
choice /C SN /T 5 /D N /M "Escolha"
if errorlevel 2 goto :use_fallback
if errorlevel 1 goto :ask_port

:ask_port
set /p PORT="Digite a porta desejada: "
if "%PORT%"=="" set PORT=%FALLBACK_PORT%
goto :start

:use_fallback
set PORT=%FALLBACK_PORT%
echo [INFO] Tempo esgotado. Usando porta %FALLBACK_PORT%.

:start
echo.
echo [INFO] Iniciando CarteiraClientes na porta %PORT%...
.\mvnw.cmd spring-boot:run -Dspring-boot.run.arguments=--server.port=%PORT%

endlocal
