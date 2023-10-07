package org.swmaestro.repl.gifthub.util;

import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;

@Getter
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SuccessMessage extends Message {
	private Object data;

	@Builder
	public SuccessMessage(int status, String path, Object data) {
		super(status, path);
		this.data = data;
	}
}
