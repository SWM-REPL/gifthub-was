package org.swmaestro.repl.gifthub.notifications;

import lombok.Getter;

@Getter
public enum NotificationType {
	EXPIRATION(0, "유효기간 임박 알림"),     // 유효기간 임박
	// RECOMMENDATION(1, "사용 추천 알림"),   // 사용 추천
	NOTICE(1, "공지사항 알림");           // 공지사항

	private final int value;
	private final String description;

	NotificationType(int value, String description) {
		this.value = value;
		this.description = description;
	}
}
