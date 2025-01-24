package com.example.camping.campingService;

public interface CampingService {
    /**
     * 사용자 주소를 기반으로 캠핑장 추천을 요청하는 메서드
     * @param userAddress 사용자 주소
     * @return 추천된 캠핑장 목록 (JSON 문자열)
     */
    String getRecommendations(String userAddress);
}
