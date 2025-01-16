package com.example.camping.entity;

import java.util.Date;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

import com.example.camping.entity.Users.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Temporal;
import jakarta.persistence.TemporalType;
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
