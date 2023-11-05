package org.swmaestro.repl.gifthub.auth.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EntityListeners;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EntityListeners(AuditingEntityListener.class)
public class Device {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private Long userId;

	@Column(length = 255, nullable = false)
	private String refreshToken;

	@Column(length = 200, nullable = false)
	private String deviceToken;

	@Column(length = 200)
	private String fcmToken;

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public Device(Long id, Long userId, String refreshToken, String deviceToken, String fcmToken) {
		this.id = id;
		this.userId = userId;
		this.refreshToken = refreshToken;
		this.deviceToken = deviceToken;
		this.fcmToken = fcmToken;
	}
}
