package org.swmaestro.repl.gifthub.filter;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

import org.springframework.stereotype.Component;
import org.springframework.web.util.ContentCachingRequestWrapper;
import org.springframework.web.util.ContentCachingResponseWrapper;
import org.springframework.web.util.WebUtils;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.extern.slf4j.Slf4j;

@Component
@Slf4j
public class LoggingFilter implements Filter {

	@Override
	public void doFilter(ServletRequest servletRequest, ServletResponse servletResponse, FilterChain chain)
			throws IOException, ServletException {

		if (servletRequest instanceof HttpServletRequest && servletResponse instanceof HttpServletResponse) {
			HttpServletRequest request = (HttpServletRequest)servletRequest;
			HttpServletResponse response = (HttpServletResponse)servletResponse;

			HttpServletRequest requestToCache = new ContentCachingRequestWrapper(request);
			HttpServletResponse responseToCache = new ContentCachingResponseWrapper(response);

			chain.doFilter(requestToCache, responseToCache);

			log.info("request header: {}", getHeaders(requestToCache));
			log.info("request body: {}", getRequestBody((ContentCachingRequestWrapper)requestToCache));

			log.info("response body: {}", getResponseBody(responseToCache));

		} else {
			chain.doFilter(servletRequest, servletResponse);
		}
	}

	private Map<String, Object> getHeaders(HttpServletRequest request) {
		Map<String, Object> headerMap = new HashMap<>();

		Enumeration<String> headerArray = request.getHeaderNames();
		while (headerArray.hasMoreElements()) {
			String headerName = headerArray.nextElement();
			headerMap.put(headerName, request.getHeader(headerName));
		}
		return headerMap;
	}

	private String getRequestBody(ContentCachingRequestWrapper request) {
		ContentCachingRequestWrapper wrapper = WebUtils.getNativeRequest(request, ContentCachingRequestWrapper.class);
		if (wrapper != null) {
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf.length > 0) {
				try {
					return new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
				} catch (UnsupportedEncodingException e) {
					return " - ";
				}
			}
		}
		return " - ";
	}

	private String getResponseBody(final HttpServletResponse response) throws IOException {
		String payload = null;
		ContentCachingResponseWrapper wrapper = WebUtils.getNativeResponse(response, ContentCachingResponseWrapper.class);
		if (wrapper != null) {
			wrapper.setCharacterEncoding("UTF-8");
			byte[] buf = wrapper.getContentAsByteArray();
			if (buf.length > 0) {
				payload = new String(buf, 0, buf.length, wrapper.getCharacterEncoding());
				wrapper.copyBodyToResponse();
			}
		}
		return null == payload ? " - " : payload;
	}
}

