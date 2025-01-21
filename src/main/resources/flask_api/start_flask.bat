REM flask api 실행 자동화 스크립트 (배포 시 사용 x, 개발단계에서만 사용)

@echo off
cd %~dp0
call python -m venv venv
call venv\Scripts\activate
pip install -r requirements.txt
python app.py
pause
