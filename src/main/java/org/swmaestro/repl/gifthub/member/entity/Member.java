package org.swmaestro.repl.gifthub.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.swmaestro.repl.gifthub.util.entity.BaseTimeEntity;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(length = 60)
	private String username;

	@NotNull
	@Column(length = 60)
	private String password;

	@NotNull
	@Column(length = 12)
	private String nickname;

	@Builder
	public Member(Long id, @NotNull String username, @NotNull String password, @NotNull String nickname) {
		this.id = id;
		this.username = username;
		this.password = password;
		this.nickname = nickname;
	}
}
