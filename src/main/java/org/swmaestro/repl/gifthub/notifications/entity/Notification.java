package org.swmaestro.repl.gifthub.notifications.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.notifications.NotificationType;
import org.swmaestro.repl.gifthub.vouchers.entity.Voucher;

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
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Notification {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "receiver_id", nullable = false)
	private Member receiver;

	@ManyToOne(optional = true)
	@JoinColumn(name = "voucher_id", nullable = true)
	private Voucher voucher;

	@Column(columnDefinition = "TINYINT", length = 1, nullable = false)
	private NotificationType type;

	@Column(columnDefinition = "TINYTEXT", length = 200, nullable = false)
	private String message;

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdAt;

	private LocalDateTime deletedAt;

	private LocalDateTime checkedAt;

	@Builder
	public Notification(Long id, Member receiver, Voucher voucher, NotificationType type, String message, LocalDateTime deletedAt, LocalDateTime checkedAt) {
		this.id = id;
		this.receiver = receiver;
		this.voucher = voucher;
		this.type = type;
		this.message = message;
		this.deletedAt = deletedAt;
		this.checkedAt = checkedAt;
	}

	public void setCheckedAt(LocalDateTime checkedAt) {
		this.checkedAt = checkedAt;
	}
}
