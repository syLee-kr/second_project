package com.example.camping.config;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Setter
@Getter
@Configuration
@ConfigurationProperties(prefix = "file")
public class FileStorageProperties {

    private String uploadDir = "C:\\Users\\user\\Desktop\\camping\\src\\main\\resources\\static\\uploads";

}
