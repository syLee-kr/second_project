package com.example.camping.entity.dto;

import lombok.Data;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;

@Data
@Document(collection = "recommended_keyword") // 원하는 컬렉션명
public class RecommendedKeyword {

    @Id
    private String id;              // MongoDB에서 자동 ObjectId로 관리
    private String columnName;      // 예: "address", "themeEnvironment", ...
    private String keyword;         // 예: "낚시", "카라반", ...
    private String label;           // 화면에 표시할 해시태그 형태의 라벨. 예: "#낚시"
}