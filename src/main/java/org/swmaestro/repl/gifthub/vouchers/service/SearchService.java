package org.swmaestro.repl.gifthub.vouchers.service;

import java.util.Base64;

import javax.annotation.PostConstruct;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.swmaestro.repl.gifthub.vouchers.dto.SearchResponseDto;

import reactor.core.publisher.Mono;

@Service
public class SearchService {
	private final WebClient openSearchClient;

	@Value("${opensearch.username}")
	private String username;

	@Value("${opensearch.password}")
	private String password;
	private String auth;

	public SearchService(WebClient.Builder webClientBuilder, @Value("${opensearch.base-url}") String baseUrl) {
		this.openSearchClient = webClientBuilder.baseUrl(baseUrl).build();
	}

	@PostConstruct
	public void init() {
		auth = "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes());
	}

	public Mono<SearchResponseDto> search(String query) {
		return openSearchClient.post()
				.uri("/product/_search")
				.header("Authorization", auth)
				.header("Content-Type", "application/json")
				.bodyValue(query)
				.retrieve()
				.bodyToMono(SearchResponseDto.class);
	}
}