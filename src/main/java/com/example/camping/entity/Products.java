package com.example.camping.entity;

import java.math.BigDecimal;
import java.time.OffsetDateTime;
import java.util.ArrayList;
import java.util.List;

import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.example.camping.CodeGenerator;

import jakarta.persistence.CascadeType;
import jakarta.persistence.CollectionTable;
import jakarta.persistence.Column;
import jakarta.persistence.ElementCollection;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.PrePersist;
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
	private String gseq; // 상품 고유 ID

	private String name; // 상품 이름

	private BigDecimal price1; // 기본 가격

	private Boolean isVisible; // 노출 여부
	private Boolean isBest; // 베스트 상품 여부
	
	@ManyToOne
	@JoinColumn(name = "category_id")
	private Category category; // 카테고리

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
    
    // 재고
    @Column(nullable = false)
    private Integer stock = 0;
    
    //gseq 코드 생성
    @PrePersist
    public void generateProductId() {
        this.gseq = CodeGenerator.generateCode(8);  // 길이를 8로 설정
    }
	
	
}
