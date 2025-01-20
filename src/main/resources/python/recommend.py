import pymongo
import pandas as pd
import numpy as np
from sklearn.feature_extraction.text import CountVectorizer
from sklearn.preprocessing import MinMaxScaler
from sklearn.metrics.pairwise import cosine_similarity

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

    # MongoDB에서 모든 캠핑장 문서를 불러와 list로 변환
    camp_docs = list(camp_collection.find({}))
    return camp_docs


##############################################
# 3) campRating 컬렉션(사용자 평점) 불러오기 #
##############################################
def load_user_ratings(user_id):
    """
    특정 사용자(userId)가 매긴 모든 평점을 불러온다.
    """
    db = get_db_connection()
    rating_collection = db["campRating"]

    # userId 필드가 일치하는 문서들을 불러오기
    rating_docs = list(rating_collection.find({"userId": user_id}))
    return rating_docs


##################################################
# 4) 캠핑장 데이터 전처리(벡터화) + DataFrame 생성 #
##################################################
def preprocess_camp_data(camp_data):
    """
    camp_data: MongoDB에서 불러온 camp 컬렉션 문서들의 list
    반환값:
       feature_df: 캠핑장 정보를 벡터화한 DataFrame (각 행 = 캠핑장)
       original_df: 원본 camp 정보를 pandas DF로 변환 (추후 유사도 결과 매핑 용)
       scaler: 수치형 변수 스케일러 (사용자 벡터 생성 시 재사용)
    """
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
        # 값이 쉼표로 구분되어 있으면 split
        df_use[col] = df_use[col].apply(lambda x: ",".join([item.strip() for item in x.split(",")]))

        vec = CountVectorizer(tokenizer=lambda x: x.split(","), binary=True)
        col_matrix = vec.fit_transform(df_use[col])
        col_df = pd.DataFrame(col_matrix.toarray(), columns=[f"{col}_{v}" for v in vec.get_feature_names_out()])
        vectorized_dfs.append(col_df)

    # 수치형(주요시설, 위도, 경도 등)
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
    from sklearn.preprocessing import MinMaxScaler
    scaler = MinMaxScaler()
    numeric_scaled = scaler.fit_transform(df_use[numeric_cols])
    numeric_scaled_df = pd.DataFrame(numeric_scaled, columns=[f"scaled_{col}" for col in numeric_cols])

    # 최종 Feature DF
    feature_df = pd.concat([numeric_scaled_df] + vectorized_dfs, axis=1)

    return feature_df, df, scaler


###################################################################################
# 5) 여러 캠핑장에 대한 사용자 평점을 바탕으로 "사용자 선호 벡터"를 만드는 헬퍼 함수 예시 #
###################################################################################
def build_user_preference_vector(user_ratings, camp_original_df, camp_feature_df, scaler):
    """
    user_ratings: 특정 유저가 평점을 매긴 campRating 문서 목록.
                  각 rating에는 { campId, rating } 등이 들어있다.
    camp_original_df: 캠핑장 원본 DF (preprocess_camp_data의 반환값 중 하나)
    camp_feature_df: 캠핑장 벡터화 DF
    scaler: camp_feature_df 생성 시 사용한 MinMaxScaler
            (여기서는 camp_feature_df 이미 스케일링된 상태이므로, re-fit은 불필요)

    구현 아이디어:
      - 사용자가 평점을 매긴 각 캠핑장(들)의 feature vector를 찾아서
      - (평점) 비례 가중 평균을 구한다. (간단한 방식)
      - 결과: (1 x feature_dim) 벡터
    """
    if len(user_ratings) == 0:
        # 평점을 하나도 매기지 않았다면, 0 벡터나 None 반환
        return np.zeros((1, camp_feature_df.shape[1]))

    # campId -> feature row 매핑용으로, _id(혹은 id) 기준 인덱스를 잡는다
    # camp_original_df의 각 행이 camp_feature_df에 매핑되는 구조이므로
    # 인덱스가 동일하다고 가정
    # 다만 MongoDB에서 넘어온 "_id"가 ObjectId(...) 형태라면 str 변환 필요.

    # 우선 _id 컬럼을 문자열로 변환해두기
    camp_original_df["_id_str"] = camp_original_df["_id"].astype(str)

    # rating마다 해당 camp를 찾아서 feature vector를 모은다
    vectors = []
    weights = []

    for r in user_ratings:
        camp_id = r.get("campId", None)  # 예: "678ddbf694756178f44123d1"
        rating_value = r.get("rating", 0.0)  # float

        # camp_original_df에서 _id_str == camp_id인 행을 찾는다
        match_index = camp_original_df.index[camp_original_df["_id_str"] == str(camp_id)]
        if len(match_index) > 0:
            idx = match_index[0]
            # camp_feature_df에서도 동일 index의 row가 해당 캠프
            camp_vec = camp_feature_df.iloc[idx].values  # numpy array 형태
            vectors.append(camp_vec)
            weights.append(rating_value)

    if len(vectors) == 0:
        # 매칭되는 캠핑장이 없는 경우
        return np.zeros((1, camp_feature_df.shape[1]))

    vectors = np.array(vectors)  # (개수, feature_dim)
    weights = np.array(weights)

    # 가중 평균(각 평점으로 가중치) -> sum(w_i * v_i) / sum(w_i)
    user_pref_vector = np.average(vectors, axis=0, weights=weights)
    user_pref_vector = user_pref_vector.reshape(1, -1)  # (1, feature_dim)
    return user_pref_vector


###################################################
# 6) 상위 N개 추천 (코사인 유사도 기준) 함수 예시 #
###################################################
def recommend_top_n(user_id, top_n=50):
    """
    1. camp 컬렉션에서 모든 캠핑장 불러오기
    2. campRating 컬렉션에서 user_id가 매긴 평점 불러오기
    3. 캠핑장 데이터 전처리(벡터화)
    4. 사용자 선호 벡터 계산(평점 가중 평균)
    5. 코사인 유사도 계산 후 상위 n개 반환
    """
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
    )  # shape: (1, feature_dim)

    # 5) 코사인 유사도
    # camp_feature_df: (캠핑장수, feature_dim)
    sim_scores = cosine_similarity(user_vec, camp_feature_df.values)  # (1, 캠핑장수)
    sim_scores = sim_scores.flatten()  # (캠핑장수, )

    # 상위 N개 인덱스
    top_indices = sim_scores.argsort()[::-1][:top_n]

    # 결과 DF
    result_df = camp_original_df.iloc[top_indices].copy()
    result_df["유사도"] = sim_scores[top_indices]

    # 유사도 순 정렬 (내림차순)
    result_df = result_df.sort_values(by="유사도", ascending=False)
    return result_df


#######################
# 7) 메인 함수 예시   #
#######################
if __name__ == "__main__":
    # 테스트용 userId
    test_user_id = "testUser"

    # 추천 실행
    recommendations = recommend_top_n(test_user_id, top_n=50)

    # 원하는 컬럼만 출력 예시
    print(recommendations[["야영장명", "유사도"]].head(50))
