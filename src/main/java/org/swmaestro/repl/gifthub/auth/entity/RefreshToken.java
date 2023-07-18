package org.swmaestro.repl.gifthub.auth.entity;

import org.swmaestro.repl.gifthub.util.BaseTimeEntity;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
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
public class RefreshToken extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(nullable = false)
	private String refreshToken;

	@Column(length = 60, nullable = false)
	private String username;

	@Builder
	public RefreshToken(Long id, String refreshToken, String username) {
		this.id = id;
		this.refreshToken = refreshToken;
		this.username = username;
	}
}
