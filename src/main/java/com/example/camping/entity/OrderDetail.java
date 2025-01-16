package com.example.camping.entity;

import org.hibernate.annotations.DynamicInsert;
import org.hibernate.annotations.DynamicUpdate;
import org.springframework.data.annotation.Id;

import com.example.camping.entity.Users.Role;

import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
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
public class OrderDetail {

		@Id
		@GeneratedValue(strategy = GenerationType.IDENTITY)
		private Long odseq; // 주문 상세 ID

		@ManyToOne
		@JoinColumn(name = "orderId", nullable = false)
		private OrderList order; // 연관된 주문

		@ManyToOne
		@JoinColumn(name = "productId", nullable = false)
		private Products product; // 연관된 상품

		private int quantity; // 주문 수량

}
