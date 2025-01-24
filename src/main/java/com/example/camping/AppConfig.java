package com.example.camping;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestTemplate;

@Configuration
public class AppConfig {
    
    // RestTemplate을 Bean으로 등록하여 애플리케이션 전역에서 사용 가능하도록 설정
    @Bean
    public RestTemplate restTemplate() {
        return new RestTemplate();
    }
}
