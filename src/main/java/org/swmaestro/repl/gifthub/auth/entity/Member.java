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
import lombok.Setter;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 60, nullable = false, unique = true)
	private String username;

	@Column(length = 60)
	private String password;

	@Column(length = 12, nullable = false)
	private String nickname;

	@Builder
	public Member(Long id, String username, String password, String nickname) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.nickname = nickname;
	}
}
