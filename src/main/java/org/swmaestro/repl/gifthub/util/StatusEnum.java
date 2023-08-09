package org.swmaestro.repl.gifthub.util;

public enum StatusEnum {
	OK(200, "OK"),
	BAD_REQUEST(400, "Bad Request"),
	UNAUTHORIZED(401, "Unauthorized"),
	FORBIDDEN(403, "Forbidden"),
	NOT_FOUND(404, "Not Found"),
	CONFLICT(409, "Conflict"),
	INTERNAL_SERVER_ERROR(500, "Internal Server Error");

	public int statusCode;
	public String code;

	StatusEnum(int statusCode, String code) {
		this.statusCode = statusCode;
		this.code = code;
	}
}
