package org.swmaestro.repl.gifthub.notifications.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor
public class FCMNotificationRequestDto {
	private Long targetMemberId;
	private String title;
	private String body;

	@Builder
	public FCMNotificationRequestDto(Long targetMemberId, String title, String body) {
		this.targetMemberId = targetMemberId;
		this.title = title;
		this.body = body;
	}
}
