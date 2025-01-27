package com.example.camping.entity.order;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import java.util.Date;

@Builder
@NoArgsConstructor
@AllArgsConstructor
@DynamicInsert
@DynamicUpdate
@Data
@Entity
public class Shipping {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long shippingId; // 배송 ID

	@OneToOne
	@JoinColumn(name = "orderId", nullable = false)
	private OrderList order; // 연관된 주문

	private String shippingStatus; // 배송 상태 (예: "배송 중", "배송 완료")
	private String trackingNumber; // 배송 추적 번호

	@Temporal(TemporalType.TIMESTAMP)
	private Date shippingDate; // 배송 날짜
}
