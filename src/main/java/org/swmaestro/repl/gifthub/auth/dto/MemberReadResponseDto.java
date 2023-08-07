package org.swmaestro.repl.gifthub.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class MemberReadResponseDto {
	private Long id;
	private String nickname;

	@Builder
	public MemberReadResponseDto(Long id, String nickname) {
		this.id = id;
		this.nickname = nickname;
	}
}
