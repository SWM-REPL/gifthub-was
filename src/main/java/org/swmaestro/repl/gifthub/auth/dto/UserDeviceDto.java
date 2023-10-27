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
public class UserDeviceDto {
	private String deviceToken;
	private String fcmToken;

	@Builder
	public UserDeviceDto(String deviceToken, String fcmToken) {
		this.deviceToken = deviceToken;
		this.fcmToken = fcmToken;
	}
}
