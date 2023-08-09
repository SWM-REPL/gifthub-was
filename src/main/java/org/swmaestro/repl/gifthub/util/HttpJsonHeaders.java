package org.swmaestro.repl.gifthub.util;

import java.nio.charset.Charset;

import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;

public class HttpJsonHeaders extends HttpHeaders {
	public HttpJsonHeaders() {
		super();
		this.setContentType(new MediaType("application", "json", Charset.forName("UTF-8")));
	}
}
