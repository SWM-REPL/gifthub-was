package org.swmaestro.repl.gifthub.giftcard.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
public class Giftcard {

	@Id
	@Column(columnDefinition = "CHAR(36)")
	private String id;

	@ManyToOne
	@JoinColumn(name = "voucher_id", nullable = false)
	private Voucher voucher;

	@Column(nullable = false, columnDefinition = "TEXT")
	private String message;

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Column(nullable = false)
	private LocalDateTime expiresAt;

	@Column(length = 60, nullable = false)
	private String password;

	@Column
	private Integer invalidPasswordCount;

	@Builder
	public Giftcard(String id, Voucher voucher, String message, LocalDateTime expiresAt, String password) {
		this.id = id;
		this.voucher = voucher;
		this.message = message;
		this.password = password;
		this.expiresAt = expiresAt;
		this.invalidPasswordCount = 0;
	}

	public Giftcard expire() {
		this.expiresAt = LocalDateTime.now();
		return this;
	}

	public Giftcard increaseInvalidPasswordCount() {
		this.invalidPasswordCount++;
		return this;

	}

	public Giftcard resetInvalidPasswordCount() {
		this.invalidPasswordCount = 0;
		return this;
	}

	public boolean isEnable() {
		return this.expiresAt.isAfter(LocalDateTime.now());
	}
}
