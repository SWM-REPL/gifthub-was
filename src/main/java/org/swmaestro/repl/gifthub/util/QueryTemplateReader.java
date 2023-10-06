package org.swmaestro.repl.gifthub.util;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

@Component
public class QueryTemplateReader {
	private static String queryTemplatePath;

	@Autowired
	public QueryTemplateReader(@Value("${query-template-path}") String queryTemplatePath) {
		this.queryTemplatePath = queryTemplatePath;
	}

	public static String readQueryTemplate() {
		try {
			return new String(Files.readAllBytes(Paths.get(queryTemplatePath)), StandardCharsets.UTF_8);
		} catch (IOException e) {
			throw new RuntimeException("쿼리 템플릿을 읽지 못했습니다.", e);
		}
	}
}