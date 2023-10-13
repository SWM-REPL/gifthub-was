package org.swmaestro.repl.gifthub.vouchers.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swmaestro.repl.gifthub.auth.entity.Member;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
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
@EntityListeners(AuditingEntityListener.class)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class VoucherUsageHistory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@ManyToOne
	@JoinColumn(name = "voucher_id", nullable = false)
	private Voucher voucher;

	@Column(nullable = true)
	private Integer amount;

	private String place;

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public VoucherUsageHistory(Long id, Member member, Voucher voucher, Integer amount, String place,
			LocalDateTime createdAt) {
		this.id = id;
		this.member = member;
		this.voucher = voucher;
		this.amount = amount;
		this.place = place;
		this.createdAt = createdAt;
	}
}
