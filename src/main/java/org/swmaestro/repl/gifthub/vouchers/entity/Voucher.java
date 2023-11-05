package org.swmaestro.repl.gifthub.vouchers.entity;

import java.time.LocalDate;
import java.time.LocalDateTime;

import org.swmaestro.repl.gifthub.auth.entity.User;
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
import lombok.Setter;

@Entity
@Getter
@Setter
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

	@Column(length = 12)
	private String barcode;

	@Column
	private Integer balance;

	@Column(nullable = false)
	private LocalDate expiresAt;

	@Column(length = 200)
	private String imageUrl;

	@ManyToOne
	@JoinColumn(name = "user_id", nullable = false)
	private User user;

	@Column
	private LocalDateTime deletedAt;

	@Builder
	public Voucher(Long id, Brand brand, Product product, String barcode, Integer balance, LocalDate expiresAt,
			String imageUrl, User user) {
		this.id = id;
		this.brand = brand;
		this.product = product;
		this.barcode = barcode;
		this.balance = balance;
		this.expiresAt = expiresAt;
		this.user = user;
		this.imageUrl = imageUrl;
	}

	public boolean isDeleted() {
		return deletedAt != null;
	}
}