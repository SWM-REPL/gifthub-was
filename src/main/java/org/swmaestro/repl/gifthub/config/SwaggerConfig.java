package org.swmaestro.repl.gifthub.config;

import org.springdoc.core.models.GroupedOpenApi;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.PropertySource;

import io.swagger.v3.oas.annotations.OpenAPIDefinition;
import io.swagger.v3.oas.annotations.info.Info;
import io.swagger.v3.oas.annotations.servers.Server;

@PropertySource("classpath:application.yml")
@OpenAPIDefinition(
		info = @Info(title = "GiftHub App",
				description = "GiftHub APP API 명세",
				version = "v1"),

		servers = {
				@Server(url = "${server.dev.url}", description = "${server.dev.description}"),
				@Server(url = "${server.local.url}", description = "${server.local.description}")
		}

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



