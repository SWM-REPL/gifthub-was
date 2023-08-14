package org.swmaestro.repl.gifthub.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AppleDto {
	private String id;
	private String email;
	private String nickname;

	@Builder
	public AppleDto(String id, String email, String nickname) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
	}
}
