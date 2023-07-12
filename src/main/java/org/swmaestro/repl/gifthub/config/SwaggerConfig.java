package org.swmaestro.repl.gifthub.config;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@OpenAPIDefinition(
	info = @Info(title = "GiftHub App",
		description = "GiftHub APP API 명세",
		version = "v1")
)
@Configuration
public class SwaggerConfig {
	@Bean
	public GroupedOpenApi OpenApi() {
		String[] paths = {"/**"};

		return GroupedOpenApi.builder()
			.group("GiftHub API v1")
			.pathsToMatch(paths)
			.build();
	}
}
