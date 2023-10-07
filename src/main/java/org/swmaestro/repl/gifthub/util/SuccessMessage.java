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
	public SuccessMessage(String path, Object data) {
		super(StatusEnum.OK.statusCode, path);
		this.data = data;
	}
}
