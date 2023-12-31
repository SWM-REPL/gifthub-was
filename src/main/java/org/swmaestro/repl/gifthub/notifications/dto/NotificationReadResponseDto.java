package org.swmaestro.repl.gifthub.notifications.dto;

import java.time.LocalDateTime;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class NotificationReadResponseDto {
	private Long id;
	private String type;
	private String message;
	private LocalDateTime notifiedAt;
	private Long voucherId;
	private LocalDateTime checkedAt;

	@Builder
	public NotificationReadResponseDto(Long id, String type, String message, LocalDateTime notifiedAt, Long voucherId,
			LocalDateTime checkedAt) {
		this.id = id;
		this.type = type;
		this.message = message;
		this.notifiedAt = notifiedAt;
		this.voucherId = voucherId;
		this.checkedAt = checkedAt;
	}
}
