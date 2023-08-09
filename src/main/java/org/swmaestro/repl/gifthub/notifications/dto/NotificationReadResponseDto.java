package org.swmaestro.repl.gifthub.notifications.dto;

import java.time.LocalDateTime;

import org.swmaestro.repl.gifthub.notifications.NotificationType;

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
	private NotificationType type;
	private String message;
	private LocalDateTime notifiedAt;
	private Long voucherId;

	@Builder
	public NotificationReadResponseDto(Long id, NotificationType type, String message, LocalDateTime notifiedAt, Long voucherId) {
		this.id = id;
		this.type = type;
		this.message = message;
		this.notifiedAt = notifiedAt;
		this.voucherId = voucherId;
	}
}
