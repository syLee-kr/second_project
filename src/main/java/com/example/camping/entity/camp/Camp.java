package com.example.camping.entity.camp;

import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;

@Document(collection = "camp")
@Data
public class Camp {

    @Id
    private String id; // MongoDB의 _id 필드와 매핑

    private Integer number; // 번호
    private String name; // 야영장명
    private String businessType; // 사업주체_구분
    private String province; // 도
    private String city; // 시군구
    private String address; // 주소
    private Integer generalCamping; // 주요시설 일반야영장
    private Integer carCamping; // 주요시설 자동차야영장
    private Integer glamping; // 주요시설 글램핑
    private Integer caravan; // 주요시설 카라반
    private Integer privateCaravan; // 주요시설 개인 카라반
    private Integer dumpStation; // 주요시설 덤프스테이션
    private String firePit; // 화로대
    private String facilities; // 부대시설
    private String nearbyFacilities; // 주변이용가능시설
    private Integer fireExtinguisherCount; // 소화기 개수
    private Integer fireWaterCount; // 방화수 개수
    private Integer fireCompanyCount; // 방화사 개수
    private Integer fireDetectorCount; // 화재감지기 개수
    private String themeEnvironment; // 테마환경
    private String equipmentRental; // 캠핑장비대여
    private String petAllowed; // 반려동물출입
    private String contact; // 연락처
    private Double latitude; // 위도
    private Double longitude; // 경도
}
