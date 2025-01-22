import pymongo
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics.pairwise import cosine_similarity

from fastapi import FastAPI
from pydantic import BaseModel
from typing import Optional, List
import uvicorn

########################
# 1) MongoDB 연결 설정 #
########################
def get_db_connection():
    client = pymongo.MongoClient("mongodb+srv://tjdduq410:J0ulHlN5uaENxk5O@cluster0.3neo2.mongodb.net/")
    db = client["camping"]  # spring.data.mongodb.database 에 적힌 DB 이름
    return db

###########################################
# 2) camp 컬렉션(캠핑장 정보) 불러오기 함수 #
###########################################
def load_camps_from_db():
    db = get_db_connection()
    camp_collection = db["camp"]
    camp_docs = list(camp_collection.find({}))
    return camp_docs

##############################################
# 3) campRating 컬렉션(사용자 평점) 불러오기 #
##############################################
def load_user_ratings(user_id):
    db = get_db_connection()
    rating_collection = db["campRating"]
    rating_docs = list(rating_collection.find({"userId": user_id}))
    return rating_docs

##################################################
# 4) 캠핑장 데이터 전처리(벡터화) + DataFrame 생성 #
##################################################
def preprocess_camp_data(camp_data):
    df = pd.DataFrame(camp_data)

    # 필요 컬럼만 추출 (실제 상황에 맞게 조정)
    use_columns = [
        "주요시설_일반야영장",
        "주요시설_자동차야영장",
        "주요시설_글램핑",
        "주요시설_카라반",
        "주요시설_개인_카라반",
        "주요시설_덤프스테이션",
        "화로대",
        "부대시설",
        "주변이용가능시설",
        "테마환경",
        "캠핑장비대여",
        "반려동물출입",
        "위도",
        "경도"
    ]

    # camp 컬렉션에 위 컬럼이 없을 수도 있으므로, 결측 처리
    for col in use_columns:
        if col not in df.columns:
            df[col] = None

    df_use = df[use_columns].copy()

    # 문자열 컬럼
    str_cols = ["화로대", "부대시설", "주변이용가능시설", "테마환경", "캠핑장비대여", "반려동물출입"]
    for col in str_cols:
        df_use[col] = df_use[col].fillna("")

    # CountVectorizer로 문자열 컬럼별 멀티핫 인코딩
    vectorized_dfs = []
    for col in str_cols:
        # 값이 쉼표로 구분되어 있다면 split 후 다시 쉼표로 join
        df_use[col] = df_use[col].apply(
            lambda x: ",".join([item.strip() for item in x.split(",")])
        )

        vec = CountVectorizer(tokenizer=lambda x: x.split(","), binary=True)
        col_matrix = vec.fit_transform(df_use[col])
        col_df = pd.DataFrame(
            col_matrix.toarray(),
            columns=[f"{col}_{v}" for v in vec.get_feature_names_out()]
        )
        vectorized_dfs.append(col_df)

    # 수치형 컬럼
    numeric_cols = [
        "주요시설_일반야영장",
        "주요시설_자동차야영장",
        "주요시설_글램핑",
        "주요시설_카라반",
        "주요시설_개인_카라반",
        "주요시설_덤프스테이션",
        "위도",
        "경도"
    ]
    for col in numeric_cols:
        df_use[col] = pd.to_numeric(df_use[col], errors="coerce").fillna(0)

    # 스케일링
    scaler = MinMaxScaler()
    numeric_scaled = scaler.fit_transform(df_use[numeric_cols])
    numeric_scaled_df = pd.DataFrame(numeric_scaled, columns=[f"scaled_{col}" for col in numeric_cols])

    # 최종 Feature DF
    feature_df = pd.concat([numeric_scaled_df] + vectorized_dfs, axis=1)

    return feature_df, df, scaler

###################################################################################
# 5) 여러 캠핑장에 대한 사용자 평점을 바탕으로 "사용자 선호 벡터"를 만드는 헬퍼 함수 #
###################################################################################
def build_user_preference_vector(user_ratings, camp_original_df, camp_feature_df, scaler):
    if len(user_ratings) == 0:
        # 평점을 하나도 매기지 않았다면, 0 벡터나 None 반환
        return np.zeros((1, camp_feature_df.shape[1]))

    # _id 컬럼을 문자열로 변환
    camp_original_df["_id_str"] = camp_original_df["_id"].astype(str)

    vectors = []
    weights = []

    for r in user_ratings:
        camp_id = r.get("campId", None)
        rating_value = r.get("rating", 0.0)

        match_index = camp_original_df.index[camp_original_df["_id_str"] == str(camp_id)]
        if len(match_index) > 0:
            idx = match_index[0]
            camp_vec = camp_feature_df.iloc[idx].values
            vectors.append(camp_vec)
            weights.append(rating_value)

    if len(vectors) == 0:
        return np.zeros((1, camp_feature_df.shape[1]))

    vectors = np.array(vectors)
    weights = np.array(weights)

    user_pref_vector = np.average(vectors, axis=0, weights=weights)
    user_pref_vector = user_pref_vector.reshape(1, -1)
    return user_pref_vector

###################################################
# 6) 상위 N개 추천 (코사인 유사도 기준) 함수 예시 #
###################################################
def recommend_top_n(user_id, top_n=50):
    # 1) 캠핑장 불러오기
    camp_data = load_camps_from_db()

    # 2) 사용자 평점 불러오기
    user_ratings = load_user_ratings(user_id)

    # 3) 전처리(벡터화)
    camp_feature_df, camp_original_df, scaler = preprocess_camp_data(camp_data)

    # 4) 사용자 선호 벡터 구하기
    user_vec = build_user_preference_vector(
        user_ratings=user_ratings,
        camp_original_df=camp_original_df,
        camp_feature_df=camp_feature_df,
        scaler=scaler
    )

    # 5) 코사인 유사도
    sim_scores = cosine_similarity(user_vec, camp_feature_df.values)
    sim_scores = sim_scores.flatten()

    # 상위 N개 인덱스
    top_indices = sim_scores.argsort()[::-1][:top_n]

    # 결과 DF
    result_df = camp_original_df.iloc[top_indices].copy()
    result_df["유사도"] = sim_scores[top_indices]
    result_df = result_df.sort_values(by="유사도", ascending=False)

    return result_df

#####################
# FastAPI 모델 정의 #
#####################
class RecommendationRequest(BaseModel):
    userId: str
    topN: Optional[int] = 50

#################
# FastAPI 초기화 #
#################
app = FastAPI()

###################################
# FastAPI 엔드포인트: 추천 요청 처리 #
###################################
@app.post("/recommend")
def recommend_api(request_data: RecommendationRequest):
    """
    POST 예시 JSON:
    {
      "userId": "testUser",
      "topN": 10
    }
    """
    user_id = request_data.userId
    top_n = request_data.topN

    recommendations = recommend_top_n(user_id, top_n=top_n)

    # 결과 DataFrame -> List of Dict
    result_list = recommendations[["야영장명", "유사도"]].head(top_n).to_dict(orient='records')

    return {"recommend": result_list}

#################
# 메인 실행 진입 #
#################
if __name__ == '__main__':
    # uvicorn을 통해 FastAPI 실행
    uvicorn.run(app, host='0.0.0.0', port=5000, reload=True)
