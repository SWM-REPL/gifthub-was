package org.swmaestro.repl.gifthub.auth.entity;

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

	@Column(nullable = false)
	private String platformId;

	@JoinColumn(name = "member_id", nullable = false)
	@ManyToOne
	private Member member;

	@Builder
	public OAuth(Long id, OAuthPlatform platform, String platformId, Member member) {
		this.id = id;
		this.platform = platform;
		this.platformId = platformId;
		this.member = member;
	}
}
