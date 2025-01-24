package com.example.camping.config;

import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Bean;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;
import java.util.ArrayList;
import java.util.List;

@Configuration
public class MongoConfig {

    @Bean
    public MongoCustomConversions customConversions() {
        List<Converter<?, ?>> converters = new ArrayList<>();
        converters.add(new OffsetDateTimeToStringConverter());
        converters.add(new StringToOffsetDateTimeConverter());
        return new MongoCustomConversions(converters);
    }

    /**
     * OffsetDateTime -> String 변환기
     */
    static class OffsetDateTimeToStringConverter implements Converter<OffsetDateTime, String> {
        @Override
        public String convert(OffsetDateTime source) {
            return source.format(DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }

    /**
     * String -> OffsetDateTime 변환기
     */
    static class StringToOffsetDateTimeConverter implements Converter<String, OffsetDateTime> {
        @Override
        public OffsetDateTime convert(String source) {
            return OffsetDateTime.parse(source, DateTimeFormatter.ISO_OFFSET_DATE_TIME);
        }
    }
}
