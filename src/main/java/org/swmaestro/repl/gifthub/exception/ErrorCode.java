package org.swmaestro.repl.gifthub.util.exception;

import com.fasterxml.jackson.annotation.JsonFormat;
import lombok.Getter;

@JsonFormat(shape = JsonFormat.Shape.OBJECT)
@Getter
// public enum ErrorCode {
// 	// Common
// 	INVALID_INPUT_VALUE(400, "C001", "Invalid Input Value"),
// 	INVALID_AUTHENTICATION(401, "C002", "Invalid Authentication"),
// 	ACCESS_DENIED(403, "C003", "Access is Denied"),
// 	NOT_FOUND_RESOURCE(404, "C004", "Not Found Resource"),
// 	EXIST_RESOURCE(409, "C005", "Exist Resource"),
// 	INTERNAL_SERVER_ERROR(500, "C006", "Internal Server Error");
//
// 	private final String code;
// 	private final String message;
// 	private int status;
//
// 	ErrorCode(final int status, final String code, final String message) {
// 		this.status = status;
// 		this.code = code;
// 		this.message = message;
// 	}
// }
