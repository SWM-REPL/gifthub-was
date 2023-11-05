package org.swmaestro.repl.gifthub.auth.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class OAuthTokenDto {
	private String token;
	private String deviceToken;
	private String fcmToken;

	@Builder
	public OAuthTokenDto(String token, String deviceToken, String fcmToken) {
		this.token = token;
		this.deviceToken = deviceToken;
		this.fcmToken = fcmToken;
	}
}
