package com.example.camping.entity;

import java.math.BigDecimal;
import java.util.Date;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;

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
public class Payment {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long paymentId; // 결제 ID

	@OneToOne
	@JoinColumn(name = "orderId", nullable = false)
	private OrderList order; // 연관된 주문

	private BigDecimal amountPaid; // 결제 금액
	private String paymentStatus; // 결제 상태 (예: "결제 완료", "대기 중")

	@Temporal(TemporalType.TIMESTAMP)
	private Date paymentDate; // 결제 날짜
}
