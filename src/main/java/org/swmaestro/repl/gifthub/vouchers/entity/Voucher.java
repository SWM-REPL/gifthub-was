package org.swmaestro.repl.gifthub.vouchers.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.util.BaseTimeEntity;

import java.time.LocalDateTime;

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

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Builder
	public Voucher(Long id, Brand brand, Product product, String barcode, int balance, LocalDateTime expiresAt,
	               String imageUrl, Member member) {
		this.id = id;
		this.brand = brand;
		this.product = product;
		this.barcode = barcode;
		this.balance = balance;
		this.expiresAt = expiresAt;
		this.member = member;
	}
}