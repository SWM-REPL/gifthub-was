package org.swmaestro.repl.gifthub.notifications.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class NoticeNotificationDto {
	private String title;
	private String body;

	@Builder
	public NoticeNotificationDto(String title, String body) {
		this.title = title;
		this.body = body;
	}
}
