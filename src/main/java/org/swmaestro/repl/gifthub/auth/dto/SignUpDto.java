package org.swmaestro.repl.gifthub.auth.dto;

import java.io.Serializable;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SignUpDto implements Serializable {
	@NotNull
	@Size(min = 4, max = 60)
	private String username;

	@NotNull
	@Size(min = 8, max = 64)
	private String password;

	@NotNull
	@Size(min = 2, max = 12)
	private String nickname;

	@NotNull
	private String deviceToken;

	private String fcmToken;

	@Builder
	public SignUpDto(String username, String password, String nickname, String deviceToken, String fcmToken) {
		this.username = username;
		this.password = password;
		this.nickname = nickname;
		this.deviceToken = deviceToken;
		this.fcmToken = fcmToken;
	}
}
