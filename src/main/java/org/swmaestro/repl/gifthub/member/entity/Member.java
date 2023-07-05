package org.swmaestro.repl.gifthub.member.entity;

import jakarta.persistence.*;
import lombok.*;
import org.swmaestro.repl.gifthub.util.entity.BaseTimeEntity;

@Entity
@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@Column(length = 60, nullable = false)
	private String username;

	@Column(length = 60, nullable = false)
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
