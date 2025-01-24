from concurrent.futures import ThreadPoolExecutor  # 병렬 처리를 위한 모듈
import requests  # API 요청을 위한 모듈
import pandas as pd  # 데이터 처리 및 저장을 위한 모듈
import os  # 파일 및 디렉토리 조작을 위한 모듈
import xml.etree.ElementTree as ET  # XML 데이터 파싱 모듈
import re  # 정규 표현식을 위한 모듈
from datetime import datetime  # 날짜 및 시간 처리를 위한 모듈
import cx_Oracle  # Oracle DB 연동 모듈
from geopy.distance import geodesic  # 거리 계산 모듈
from geopy.geocoders import Nominatim  # 주소를 좌표로 변환하는 모듈

# API 및 데이터베이스 설정
API_KEY = "oIwumC5H/+uYRsDUXYeOrHeKysDdaKhpoPVUO/ORYw+Er02WZB1oGR935nR7GqKV2g9hrSN13Hp69yvDZdNxuA=="
BASE_URL = "http://apis.data.go.kr/B551011/GoCamping/basedList"
CACHE_FILE = "camping_data_cache.csv"

DB_CONFIG = {
    "user": "camp",
    "password": "camp123",
    "dsn": "localhost:1521/XE"
}

def clean_xml_response(response_text):
    # XML 응답의 특수 문자를 정리하는 함수
    response_text = re.sub(r"[^\x09\x0A\x0D\x20-\x7F]", "", response_text)
    response_text = response_text.replace("&", "&amp;")
    return response_text

def fetch_page_data(page_no):
    # 특정 페이지의 캠핑장 정보를 가져오는 함수
    params = {
        "serviceKey": API_KEY,
        "numOfRows": 1000,
        "pageNo": page_no,
        "MobileOS": "ETC",
        "MobileApp": "CampingApp",
        "_type": "json"  # JSON 응답을 보장
    }
    response = requests.get(BASE_URL, params=params)
    if response.status_code != 200:
        print(f"API 요청 실패: {response.status_code}")
        return []

    try:
        return response.json().get('response', {}).get('body', {}).get('items', {}).get('item', [])
    except Exception as e:
        print(f"JSON 응답 처리 오류: {e}")
        return []

def fetch_all_camping_data():
    # 모든 캠핑장 데이터를 가져오고 캐시 처리하는 함수
    if os.path.exists(CACHE_FILE):
        last_modified = datetime.fromtimestamp(os.path.getmtime(CACHE_FILE))
        if (datetime.now() - last_modified).days < 1:
            return pd.read_csv(CACHE_FILE)

    total_pages = 5  # 전체 페이지 수 지정
    with ThreadPoolExecutor(max_workers=5) as executor:
        all_data = executor.map(fetch_page_data, range(1, total_pages + 1))

    flat_data = [item for sublist in all_data for item in sublist]
    df = pd.DataFrame(flat_data)
    df.to_csv(CACHE_FILE, index=False)
    return df

def get_user_address(user_id):
    # 데이터베이스에서 사용자 주소를 조회하는 함수
    try:
        conn = cx_Oracle.connect(**DB_CONFIG)
        cursor = conn.cursor()
        query = "SELECT city, district, detailed_address FROM users WHERE user_id = :user_id"
        cursor.execute(query, user_id=user_id)
        result = cursor.fetchone()
        return f"{result[0]} {result[1]} {result[2]}" if result else None
    except cx_Oracle.DatabaseError as e:
        return None
    finally:
        if cursor:
            cursor.close()
        if conn:
            conn.close()

def recommend_camping_sites_with_map(user_id):
    # 사용자에게 가장 가까운 캠핑장 5곳을 추천하는 함수
    user_address = get_user_address(user_id)
    if not user_address:
        return {"error": "사용자를 찾을 수 없습니다."}

    geolocator = Nominatim(user_agent="geoapi", timeout=10)
    location = geolocator.geocode(user_address)
    if not location:
        return {"error": "주소 변환 실패"}

    camping_data = fetch_all_camping_data()
    camping_data[["mapX", "mapY"]] = camping_data[["mapX", "mapY"]].apply(pd.to_numeric, errors='coerce')
    camping_data.dropna(subset=["mapX", "mapY"], inplace=True)
    camping_data["distance"] = camping_data.apply(lambda row: geodesic((location.latitude, location.longitude), (row["mapY"], row["mapX"])).kilometers, axis=1)

    top_campsites = camping_data.nsmallest(5, "distance")
    return {"recommendations": top_campsites[["facltNm", "addr1", "distance"]].to_dict(orient="records")}
