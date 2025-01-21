package com.example.camping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

import com.example.camping.campingService.FlaskService;

@SpringBootApplication
public class CampingApplication {
	
	private final FlaskService flaskService;

    public CampingApplication(FlaskService flaskService) {
        this.flaskService = flaskService;
    }

	public static void main(String[] args) {
		SpringApplication.run(CampingApplication.class, args);
	}

}
