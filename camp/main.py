import logging
from bson import ObjectId
from fastapi import FastAPI, Request
from pydantic import BaseModel
from typing import List, Dict, Any, Optional
import uvicorn

import pandas as pd
import numpy as np

from sklearn.ensemble import RandomForestRegressor
from sklearn.preprocessing import MultiLabelBinarizer

from pymongo import MongoClient

# ----- 로거 설정 -----
logger = logging.getLogger(__name__)
logger.setLevel(logging.DEBUG)

app = FastAPI()

# ----- MongoDB 연결 -----
MONGO_URI = "mongodb+srv://tjdduq410:J0ulHlN5uaENxk5O@cluster0.3neo2.mongodb.net/"
client = MongoClient(MONGO_URI)
db = client["camping"]
camp_collection = db["camp"]
rating_collection = db["campRating"]


class RecommendRequest(BaseModel):
    userId: str
    topN: int = 30


def parse_comma_separated(value: Optional[str]) -> List[str]:
    """쉼표 구분 문자열 -> 리스트"""
    if not value:
        return []
    return [v.strip() for v in value.split(",") if v.strip()]


def convert_pet_allowed(value: Optional[str]) -> int:
    """'가능'/'불가능' -> 1/0"""
    if value == "가능":
        return 1
    return 0


def extract_features(camp: dict) -> dict:
    """camp 문서에서 사용하고자 하는 feature들을 추출하여 dict로 반환"""
    features = {}
    city = camp.get("시군구", "")
    features["city"] = city

    glamping = camp.get("주요시설 글램핑", 0)
    caravan = camp.get("주요시설 카라반", 0)
    features["glamping"] = glamping
    features["caravan"] = caravan

    facilities = parse_comma_separated(camp.get("부대시설", None))
    nearby = parse_comma_separated(camp.get("주변이용가능시설", None))
    features["facilities"] = facilities
    features["nearby"] = nearby

    theme_env = parse_comma_separated(camp.get("테마환경", None))
    features["themeEnvironment"] = theme_env

    equipment = parse_comma_separated(camp.get("캠핑장비대여", None))
    features["equipmentRental"] = equipment

    pet = convert_pet_allowed(camp.get("반려동물출입", None))
    features["petAllowed"] = pet

    return features


@app.post("/recommend")
def recommend(request_data: RecommendRequest):
    user_id = request_data.userId
    top_n = request_data.topN

    logger.info(f"/recommend called with userId={user_id}, topN={top_n}")

    # 1) 유저 평점 가져오기
    user_ratings_cursor = rating_collection.find({"userId": user_id})
    user_ratings = list(user_ratings_cursor)
    logger.debug(f"user_id={user_id}, user_ratings 개수={len(user_ratings)}")

    if len(user_ratings) == 0:
        logger.info("user_ratings가 0건이므로 Cold-start. 빈 결과 반환.")
        return {"recommend": []}

    # 2) 캠프 정보 가져오기
    all_camps_cursor = camp_collection.find({})
    all_camps = list(all_camps_cursor)
    logger.debug(f"camp 컬렉션 전체 문서 수: {len(all_camps)}")

    # campId -> campDoc 매핑
    camp_map = {str(doc["_id"]): doc for doc in all_camps}
    logger.debug(f"camp_map 생성 완료. 키 개수={len(camp_map)} (예: {list(camp_map.keys())[:5]})")

    # 3) 학습 데이터 X, y 생성
    train_data = []
    train_labels = []

    for r in user_ratings:
        c_id = r["campId"]
        # 만약 campId가 ObjectId라면 문자열로 변환
        if isinstance(c_id, ObjectId):
            c_id = str(c_id)

        rating_value = r["rating"]
        logger.debug(f"rating doc => campId={c_id}, rating={rating_value}")

        camp_doc = camp_map.get(c_id)
        if not camp_doc:
            logger.warning(f"camp_map에서 c_id={c_id} 키를 찾지 못함 -> 이 평점 무시")
            continue

        logger.debug(f"camp_map에서 c_id={c_id} 문서를 찾았습니다. 예: {camp_doc.get('야영장명')}")
        features = extract_features(camp_doc)
        train_data.append(features)
        train_labels.append(rating_value)

    logger.debug(f"최종 train_data 개수={len(train_data)}, train_labels 개수={len(train_labels)}")

    if len(train_data) == 0:
        logger.info("매칭되는 camp 문서가 없어 학습 데이터가 0건. 빈 결과 반환.")
        return {"recommend": []}

    # 4) Feature Engineering
    df = pd.DataFrame(train_data)
    logger.debug(f"train_data DataFrame columns: {df.columns.tolist()}")
    logger.debug(f"df.head():\n{df.head()}")

    # city -> OneHot
    city_ohe = pd.get_dummies(df["city"], prefix="city")
    numeric_cols = df[["glamping", "caravan", "petAllowed"]].fillna(0)

    mlb_facilities = MultiLabelBinarizer()
    facilities_ohe = pd.DataFrame(
        mlb_facilities.fit_transform(df["facilities"]),
        columns=[f"fac_{c}" for c in mlb_facilities.classes_]
    )

    mlb_nearby = MultiLabelBinarizer()
    nearby_ohe = pd.DataFrame(
        mlb_nearby.fit_transform(df["nearby"]),
        columns=[f"near_{c}" for c in mlb_nearby.classes_]
    )

    mlb_theme = MultiLabelBinarizer()
    theme_ohe = pd.DataFrame(
        mlb_theme.fit_transform(df["themeEnvironment"]),
        columns=[f"theme_{c}" for c in mlb_theme.classes_]
    )

    mlb_equip = MultiLabelBinarizer()
    equip_ohe = pd.DataFrame(
        mlb_equip.fit_transform(df["equipmentRental"]),
        columns=[f"equip_{c}" for c in mlb_equip.classes_]
    )

    X = pd.concat(
        [city_ohe, numeric_cols, facilities_ohe, nearby_ohe, theme_ohe, equip_ohe],
        axis=1
    )
    y = np.array(train_labels)

    logger.debug(f"X.shape={X.shape}, y.shape={y.shape}")
    logger.debug(f"X columns={X.columns.tolist()}")

    # 5) 모델 학습
    if len(X) < 2:
        logger.info("학습 데이터가 2건 미만 -> 모델 학습 불가. 빈 결과 반환.")
        return {"recommend": []}

    model = RandomForestRegressor(n_estimators=100, random_state=42)
    model.fit(X, y)
    logger.info("RandomForestRegressor 학습 완료.")

    # 6) 예측 대상
    predict_data = []
    predict_camp_ids = []
    already_rated_ids = [str(r["campId"]) for r in user_ratings]

    for camp_doc in all_camps:
        camp_id_str = str(camp_doc["_id"])
        if camp_id_str in already_rated_ids:
            # 이미 평점을 매긴 캠프는 제외
            continue

        feats = extract_features(camp_doc)
        predict_data.append(feats)
        predict_camp_ids.append(camp_id_str)

    logger.debug(f"예측 대상 캠프 수={len(predict_data)}")

    if not predict_data:
        logger.info("예측 대상 캠프가 없음(모두 평가했을 가능성). 빈 결과 반환.")
        return {"recommend": []}

    pred_df = pd.DataFrame(predict_data)
    logger.debug(f"pred_df.shape={pred_df.shape}, columns={pred_df.columns.tolist()}")

    # city OHE
    pred_city_ohe = pd.get_dummies(pred_df["city"], prefix="city")

    # 학습된 city_ohe.columns와 맞춰서 부재 컬럼은 0으로, 불필요 컬럼은 드롭
    for col in city_ohe.columns:
        if col not in pred_city_ohe.columns:
            pred_city_ohe[col] = 0
    for col in pred_city_ohe.columns:
        if col not in city_ohe.columns:
            pred_city_ohe.drop(col, axis=1, inplace=True)

    pred_city_ohe = pred_city_ohe[city_ohe.columns]

    pred_numeric_cols = pred_df[["glamping", "caravan", "petAllowed"]].fillna(0)

    # transform
    fac_pred = mlb_facilities.transform(pred_df["facilities"])
    facilities_ohe_pred = pd.DataFrame(
        fac_pred, columns=[f"fac_{c}" for c in mlb_facilities.classes_]
    )

    near_pred = mlb_nearby.transform(pred_df["nearby"])
    nearby_ohe_pred = pd.DataFrame(
        near_pred, columns=[f"near_{c}" for c in mlb_nearby.classes_]
    )

    theme_pred = mlb_theme.transform(pred_df["themeEnvironment"])
    theme_ohe_pred = pd.DataFrame(
        theme_pred, columns=[f"theme_{c}" for c in mlb_theme.classes_]
    )

    equip_pred = mlb_equip.transform(pred_df["equipmentRental"])
    equip_ohe_pred = pd.DataFrame(
        equip_pred, columns=[f"equip_{c}" for c in mlb_equip.classes_]
    )

    X_pred = pd.concat(
        [
            pred_city_ohe,
            pred_numeric_cols,
            facilities_ohe_pred,
            nearby_ohe_pred,
            theme_ohe_pred,
            equip_ohe_pred,
        ],
        axis=1
    )

    logger.debug(f"X_pred.shape={X_pred.shape} for 예측.")

    predicted_ratings = model.predict(X_pred)

    # 결과 정렬
    results = []
    for i, cid in enumerate(predict_camp_ids):
        camp_doc = camp_map.get(cid)
        if not camp_doc:
            continue
        camp_name = camp_doc.get("야영장명", "")
        results.append({
            "campId": cid,
            "야영장명": camp_name,
            "predicted_rating": round(predicted_ratings[i], 2)
        })

    results_sorted = sorted(results, key=lambda x: x["predicted_rating"], reverse=True)
    topN_result = results_sorted[:top_n]

    logger.info(f"최종 추천 결과 {len(topN_result)}건 반환.")

    return {"recommend": topN_result}
