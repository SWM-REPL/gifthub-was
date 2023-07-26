package org.swmaestro.repl.gifthub.auth.entity;

import java.time.LocalDateTime;

import org.springframework.data.annotation.CreatedDate;

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
public class UserToken {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "member_id", nullable = false)
	private Member member;

	@Column(length = 100)
	private String token;

	@CreatedDate
	@Column(nullable = false)
	private LocalDateTime createdAt;

	@Builder
	public UserToken(Long id, Member member, String token, LocalDateTime createdAt) {
		this.id = id;
		this.member = member;
		this.token = token;
		this.createdAt = createdAt;
	}
}