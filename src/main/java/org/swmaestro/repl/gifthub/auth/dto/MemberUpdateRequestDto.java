package org.swmaestro.repl.gifthub.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MemberUpdateRequestDto {
	private String nickname;
	private String password;

	@Builder
	public MemberUpdateRequestDto(String nickname, String password) {
		this.nickname = nickname;
		this.password = password;
	}
}
