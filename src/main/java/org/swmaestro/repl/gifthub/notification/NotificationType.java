package org.swmaestro.repl.gifthub.notification;

import lombok.Getter;

@Getter
public enum NotificationType {
	EXPIRATION(0),     // 유효기간 임박
	RECOMMENDATION(1);   // 사용 추천

	private final int value;

	NotificationType(int value) {
		this.value = value;
	}
}
