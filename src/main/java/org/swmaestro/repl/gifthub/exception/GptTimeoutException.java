package org.swmaestro.repl.gifthub.exception;

import org.swmaestro.repl.gifthub.util.StatusEnum;

public class GptTimeoutException extends BusinessException {
	public GptTimeoutException() {
		super("GPT 요청이 시간초과되었습니다.", StatusEnum.NOT_FOUND);
	}
}
