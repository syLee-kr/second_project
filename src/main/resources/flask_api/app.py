import threading  # 자동 종료를 위한 스레드 모듈
import time  # 타이머 설정 모듈
from flask import Flask, jsonify, request  # Flask 프레임워크 및 JSON 처리를 위한 모듈
from recommend import recommend_camping_sites_with_map  # 캠핑장 추천 기능 임포트

# Flask 애플리케이션 초기화
app = Flask(__name__)

def auto_shutdown():
    # 10분 후 Flask 서버 자동 종료
    time.sleep(600)  # 600초 대기 후 종료
    shutdown_func = request.environ.get('werkzeug.server.shutdown')  # 서버 종료 함수 가져오기
    if shutdown_func is not None:
        shutdown_func()  # 종료 함수 실행
    print("Flask 서버가 자동으로 종료되었습니다.")

@app.route('/recommend', methods=['GET'])
def recommend():
    # 사용자 ID를 기반으로 캠핑장 추천을 제공하는 API 엔드포인트
    user_id = request.args.get('userId')  # 클라이언트 요청에서 'userId' 파라미터 가져오기
    if not user_id:
        return jsonify({'error': 'User ID가 필요합니다.'}), 400  # 사용자 ID 없을 경우 오류 반환

    result = recommend_camping_sites_with_map(user_id)  # 추천 로직 실행
    if "error" in result:
        return jsonify(result), 400  # 추천 중 오류 발생 시 반환

    return jsonify(result)  # 추천 결과 반환

@app.route('/')
def home():
    # 기본 페이지 경로
    return "캠핑 추천 시스템 실행 중."

if __name__ == "__main__":
    print("Flask 캠핑 추천 서비스 시작 중...")
    threading.Thread(target=auto_shutdown).start()  # 자동 종료를 위한 스레드 시작
    app.run(host="0.0.0.0", port=5000, debug=False)  # 모든 네트워크에서 접속 가능하도록 설정 후 실행
