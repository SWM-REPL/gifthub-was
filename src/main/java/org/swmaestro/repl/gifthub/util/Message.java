package org.swmaestro.repl.gifthub.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class Message {
	private Timestamp timestamp;
	private int status;
	private String path;

	@Builder
	public Message(int status, String path) {
		this.status = status;
		this.path = path;
		this.timestamp = new Timestamp(getKoreanTime().toInstant().toEpochMilli());
	}

	private ZonedDateTime getKoreanTime() {
		long timestamp = System.currentTimeMillis();
		Instant instant = Instant.ofEpochMilli(timestamp);
		ZoneId koreaZone = ZoneId.of("Asia/Seoul");
		ZonedDateTime koreaTime = instant.atZone(koreaZone);
		return koreaTime;
	}
}
