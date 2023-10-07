package org.swmaestro.repl.gifthub.util;

import java.sql.Timestamp;

import jakarta.persistence.MappedSuperclass;
import lombok.Builder;
import lombok.Getter;

@MappedSuperclass
@Getter
public abstract class Msg {
	private Timestamp timestamp;
	private int status;
	private String path;

	@Builder
	public Msg(Timestamp timestamp, int status, String path) {
		this.timestamp = timestamp;
		this.status = status;
		this.path = path;
	}
}
