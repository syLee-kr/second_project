from flask import Flask, jsonify, request  # Flask 웹 프레임워크를 사용한 API 구축
import threading  # 백그라운드에서 자동 종료 실행을 위한 스레드 사용
import time  # 자동 종료 타이머를 위한 시간 모듈
from recommend import recommend_camping_sites  # 추천 로직을 포함한 별도 모듈 임포트

# flask 메인 실행 로직 

app = Flask(__name__)  # Flask 애플리케이션 초기화

# 일정 시간 후 자동 종료 로직 (예: 10분 후 자동 종료)
def auto_shutdown():
    time.sleep(600)  # 600초(10분) 동안 대기 후 종료
    shutdown_func = request.environ.get('werkzeug.server.shutdown')
    if shutdown_func is not None:
        shutdown_func()
    print("Flask 서버가 자동으로 종료되었습니다.")

# 추천 캠핑장 API 엔드포인트
@app.route('/recommend', methods=['GET'])
def recommend():
    # 클라이언트 요청에서 'address' 파라미터 가져오기
    user_address = request.args.get('address')
    if not user_address:
        return jsonify({'error': 'Address parameter is required'}), 400  # 주소가 없을 경우 에러 반환

    # 추천 로직 실행
    recommendations = recommend_camping_sites(user_address)
    return jsonify(recommendations)  # 추천 결과를 JSON 형태로 반환

# 기본 페이지
@app.route('/')
def home():
    return "캠핑장 추천 시스템 실행 중"

if __name__ == "__main__":
    print("Flask 캠핑 추천 서비스 시작 중...")
    threading.Thread(target=auto_shutdown).start()  # 10분 후 자동 종료를 위한 스레드 시작
    app.run(host="0.0.0.0", port=5000, debug=True)  # 모든 네트워크에서 접속 가능하도록 설정 후 실행
