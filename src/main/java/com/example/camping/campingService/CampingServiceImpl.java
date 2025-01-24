package com.example.camping.campingService;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CampingServiceImpl implements CampingService {

    // Flask API URL, 추후 환경 설정 파일(application.properties)로 분리 가능
    private static final String FLASK_API_URL = "http://127.0.0.1:5000/recommend?address=";

    private final RestTemplate restTemplate;

    // 생성자를 통한 의존성 주입 (테스트 및 유지보수 용이)
    public CampingServiceImpl(RestTemplate restTemplate) {
        this.restTemplate = restTemplate;
    }

    @Override
    public String getRecommendations(String userAddress) {
        // API 요청 URL 생성
        String url = FLASK_API_URL + userAddress;

        try {
            // Flask API 호출 및 응답 받기
            return restTemplate.getForObject(url, String.class);
        } catch (Exception e) {
            // 오류 발생 시 로그 출력 및 예외 처리
            System.err.println("Flask API 호출 실패: " + e.getMessage());
            return "추천 정보를 가져올 수 없습니다.";
        }
    }
}
