package org.swmaestro.repl.gifthub.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserUpdateRequestDto {
	private String nickname;
	private String password;

	@Builder
	public UserUpdateRequestDto(String nickname, String password) {
		this.nickname = nickname;
		this.password = password;
	}
}
