package org.swmaestro.repl.gifthub.vouchers.entity;

import java.time.LocalDateTime;

import org.swmaestro.repl.gifthub.util.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Voucher extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@ManyToOne
	@JoinColumn(name = "product_id", nullable = false)
	private Product product;

	@Column(length = 12, nullable = false)
	private String barcode;

	@Column(nullable = false)
	private int balance;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(length = 100)
	private String imageUrl;

	@Builder
	public Voucher(Long id, Brand brand, Product product, String barcode, int balance, LocalDateTime expiresAt,
		String imageUrl) {
		this.id = id;
		this.brand = brand;
		this.product = product;
		this.barcode = barcode;
		this.balance = balance;
		this.expiresAt = expiresAt;
	}
}