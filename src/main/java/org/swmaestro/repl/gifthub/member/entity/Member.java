package org.swmaestro.repl.gifthub.member.entity;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotNull;
import org.swmaestro.repl.gifthub.util.entity.BaseTimeEntity;

@Entity

public class Member extends BaseTimeEntity {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@NotNull
	@Column(length = 60)
	private String password;

	@NotNull
	@Column(length = 12)
	private String nickname;
}
