package com.example.camping.entity.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class OrderList {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long orderId; // 주문 ID

	private String userId;

	@Temporal(TemporalType.TIMESTAMP)
	private Date orderDate; // 주문 날짜

	@OneToMany(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private final List<OrderDetail> orderDetails = new ArrayList<>(); // 주문 상세 목록

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private Payment payment; // 결제 정보

	@OneToOne(mappedBy = "order", cascade = CascadeType.ALL, orphanRemoval = true)
	private Shipping shipping; // 배송 정보


}
