REM flask api 실행 자동화 스크립트 

@echo off
cd /d %~dp0

:: Python 실행 경로 설정 (환경변수에 등록되지 않은 경우 절대 경로 사용 가능)
set PYTHON_EXEC=python
:: 예시: set PYTHON_EXEC=C:\Users\jeahe\AppData\Local\Programs\Python\Python312\python.exe

:: Flask 서버 실행 확인
echo Flask 서버를 실행합니다...
%PYTHON_EXEC% app.py > flask_log.txt 2>&1

:: 실행 후 에러 확인
if %errorlevel% neq 0 (
    echo Flask 서버 실행 중 오류가 발생했습니다. flask_log.txt 파일을 확인하세요.
) else (
    echo Flask 서버가 정상적으로 시작되었습니다.
)

:: 포트 5000 충돌 확인
netstat -ano | findstr :5000
if %errorlevel% equ 0 (
    echo 포트 5000이 이미 사용 중입니다. 프로세스를 확인하고 종료 후 다시 시도하세요.
    exit
)

pause
