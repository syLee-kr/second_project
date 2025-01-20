package com.example.camping;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.data.mongodb.config.EnableMongoAuditing;

@SpringBootApplication
@EnableMongoAuditing
public class CampingApplication {

    public static void main(String[] args) {
        SpringApplication.run(CampingApplication.class, args);
    }

}
