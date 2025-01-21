package com.example.camping.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Products {

	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long pseq; // 상품 고유 ID

	private String name; // 상품 이름

	private BigDecimal price1; // 기본 가격
	private BigDecimal price2; // 할인 전 가격
	private BigDecimal discount; // 할인율 (예: 0.1 = 10% 할인)

	private Boolean isVisible; // 노출 여부
	private Boolean isBest; // 베스트 상품 여부
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category; // 카테고리

	// 할인 적용 후 가격 계산
	public BigDecimal getPrice3() {
	    if (price1 == null || discount == null) {
	        return BigDecimal.ZERO;
	    }
	    return price1.subtract(price1.multiply(discount));
	}
	// 상품 설명
	private String content;
	
	//이미지
	@ElementCollection
    @CollectionTable(name = "product_images", joinColumns = @JoinColumn(name = "product_id"))
    @Column(columnDefinition = "varchar2(255)")
    private List<String> imagePaths = new ArrayList<>();   // 상품 이미지 경로 리스트
	
	//사용여부
	@Column(columnDefinition="char(1) default 'y'")
	private String useyn;
	
	//베스트상품
	@Column(columnDefinition="char(1) default 'n'")
	private String bestyn;

	//등록일자
	@CreationTimestamp              
	@Column(updatable = false)
	private OffsetDateTime regdate;
	
	// 조회수
    @Column(nullable = false, columnDefinition = "INT DEFAULT 0")
    private Long cnt; // 조회수
	
	
	
}
