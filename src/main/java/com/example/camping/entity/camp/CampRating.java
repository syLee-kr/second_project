package com.example.camping.entity.camp;

import lombok.AllArgsConstructor;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import java.time.OffsetDateTime;

@Document(collection = "campRating")
@Data
public class CampRating {

    @Id
    private String id;

    private String userId;  // 사용자 ID (Users.userId와 매핑)
    private String campId;  // 캠핑장 ID (Camp.id와 매핑)
    private double rating;  // 평점 (1~10)
    private String review;  // 리뷰(문자열)

    private OffsetDateTime ratingDate; // 평점 작성일
}