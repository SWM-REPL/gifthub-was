package org.swmaestro.repl.gifthub.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class NaverDto {
	private String id;
	private String email;
	private String nickname;

	@Builder
	public NaverDto(String id, String email, String nickname) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
	}
}
