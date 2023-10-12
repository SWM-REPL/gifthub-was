package org.swmaestro.repl.gifthub.auth.entity;

import java.time.LocalDateTime;

import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;

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
public class OAuth {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(columnDefinition = "TINYINT", nullable = false)
	private OAuthPlatform platform;

	@Column(nullable = false, unique = true)
	private String platformId;

	@Column
	private String email;

	@Column
	private String nickname;

	@JoinColumn(name = "member_id", nullable = false)
	@ManyToOne
	private Member member;

	@Column
	private LocalDateTime deletedAt;

	@Builder
	public OAuth(OAuthPlatform platform, String platformId, String email, String nickname, Member member) {
		this.platform = platform;
		this.platformId = platformId;
		this.email = email;
		this.nickname = nickname;
		this.member = member;
	}
}
