package org.swmaestro.repl.gifthub.auth.dto;

import java.util.List;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class UserInfoResponseDto {
	private Long id;
	private String username;
	private String nickname;
	private List<OAuthUserInfoDto> oauth;
	public boolean allowNotifications;

	@Builder
	public UserInfoResponseDto(Long id, String username, String nickname, List<OAuthUserInfoDto> oauth, boolean allowNotifications) {
		this.id = id;
		this.username = username;
		this.nickname = nickname;
		this.oauth = oauth;
		this.allowNotifications = allowNotifications;
	}
}
