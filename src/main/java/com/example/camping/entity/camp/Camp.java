package com.example.camping.entity.camp;

import org.bson.types.ObjectId;
import org.springframework.data.annotation.Id;
import org.springframework.data.mongodb.core.mapping.Document;
import lombok.Data;
import org.springframework.data.mongodb.core.mapping.Field;

@Document(collection = "camp")
@Data
public class Camp {

    @Id
    @Field("_id")
    private ObjectId id;

    public String getIdHex() {
        return id != null ? id.toHexString() : null;
    }

    @Field("번호")
    private Integer number;

    @Field("야영장명")
    private String name;

    @Field("사업주체_구분")
    private String businessType;

    @Field("도")
    private String province;

    @Field("시군구")
    private String city;

    @Field("주소")
    private String address;

    @Field("주요시설 일반야영장")
    private Integer generalCamping;

    @Field("주요시설 자동차야영장")
    private Integer carCamping;

    @Field("주요시설 글램핑")
    private Integer glamping;

    @Field("주요시설 카라반")
    private Integer caravan;

    @Field("주요시설 개인 카라반")
    private Integer privateCaravan;

    @Field("주요시설 덤프스테이션")
    private Integer dumpStation;

    @Field("화로대")
    private String firePit;

    @Field("부대시설")
    private String facilities;

    @Field("주변이용가능시설")
    private String nearbyFacilities;

    @Field("소화기 개수")
    private Integer fireExtinguisherCount;

    @Field("방화수 개수")
    private Integer fireWaterCount;

    @Field("방화사 개수")
    private Integer fireCompanyCount;

    @Field("화재감지기 개수")
    private Integer fireDetectorCount;

    @Field("테마환경")
    private String themeEnvironment;

    @Field("캠핑장비대여")
    private String equipmentRental;

    @Field("반려동물출입")
    private String petAllowed;

    @Field("연락처")
    private String contact;

    @Field("위도")
    private Double latitude;

    @Field("경도")
    private Double longitude;
}

