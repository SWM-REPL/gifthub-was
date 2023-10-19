package org.swmaestro.repl.gifthub.util;

import java.util.Base64;

import jakarta.servlet.http.HttpServletRequest;

public class BasicAuthenticationDecoder {
	public static String decode(HttpServletRequest request) {
		String header = request.getHeader("Authorization");
		return new String(Base64.getDecoder().decode(header.replace("Basic ", "")));
	}
}
