package org.swmaestro.repl.gifthub.config;

import java.time.Duration;

import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.client.reactive.ReactorClientHttpConnector;
import org.springframework.web.reactive.function.client.WebClient;

import reactor.netty.http.client.HttpClient;
import reactor.netty.resources.ConnectionProvider;

@Configuration
public class WebClientConfig {
	@Bean
	public WebClient webClient() {
		return WebClient.builder()
				.clientConnector(new ReactorClientHttpConnector(httpClient))
				.build();
	}

	HttpClient httpClient = HttpClient.create(
			ConnectionProvider.builder("gifthub-connections")
					.maxConnections(100)
					.maxIdleTime(Duration.ofSeconds(30))
					.pendingAcquireTimeout(Duration.ofSeconds(45))
					.pendingAcquireMaxCount(-1)
					.evictInBackground(Duration.ofSeconds(30))
					.lifo()
					.build()
	);
}
