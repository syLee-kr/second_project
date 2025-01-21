package com.example.camping.campingService;

import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
public class CampingServiceImpl implements CampingService {

	//flask api 호출 로직 (flaskService를 통해서 api 호출) 
    private final String FLASK_API_URL = "http://127.0.0.1:5000/recommend?address=";

    @Override
    public String getRecommendations(String userAddress) {
        RestTemplate restTemplate = new RestTemplate();
        String url = FLASK_API_URL + userAddress;
        return restTemplate.getForObject(url, String.class);
    }
}
