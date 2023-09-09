package org.swmaestro.repl.gifthub.notifications.dto;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class DeviceTokenSaveRequestDto {
	private String token;

	@Builder
	public DeviceTokenSaveRequestDto(String token) {
		this.token = token;
	}
}
