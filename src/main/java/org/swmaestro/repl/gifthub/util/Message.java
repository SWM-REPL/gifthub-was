package org.swmaestro.repl.gifthub.util;

import java.sql.Timestamp;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;

import jakarta.persistence.MappedSuperclass;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class Message {
	private Timestamp timestamp;
	private int status;
	private String path;

	public Message(int status, String path) {
		this.timestamp = Timestamp.from(getKoreanTime().toInstant());
		this.status = status;
		this.path = path;
	}

	public Message(int status, String path, Timestamp timestamp) {
		this.timestamp = getKoreanTime();
		this.status = status;
		this.path = path;
	}

	private Timestamp getKoreanTime() {
		long timestamp = System.currentTimeMillis();
		Instant instant = Instant.ofEpochMilli(timestamp);
		ZoneId koreaZone = ZoneId.of("Asia/Seoul");
		ZonedDateTime koreaTime = instant.atZone(koreaZone);
		return Timestamp.from(koreaTime.toInstant());
	}
}
