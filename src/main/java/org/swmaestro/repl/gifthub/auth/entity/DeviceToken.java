package org.swmaestro.repl.gifthub.auth.entity;

import jakarta.persistence.*;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.springframework.data.annotation.CreatedDate;

import java.time.LocalDateTime;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class DeviceToken {
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
	public DeviceToken(Long id, Member member, String token, LocalDateTime createdAt) {
		this.id = id;
		this.member = member;
		this.token = token;
		this.createdAt = createdAt;
	}
}