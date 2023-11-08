package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class GptResponseException extends BusinessException {
	public GptResponseException() {
		super("GPT 응답이 올바르지 않습니다.", StatusEnum.NOT_FOUND);
	}
}