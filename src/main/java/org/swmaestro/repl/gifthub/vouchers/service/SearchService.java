package org.swmaestro.repl.gifthub.vouchers.service;

import java.util.Base64;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.config.WebClientConfig;
import org.swmaestro.repl.gifthub.vouchers.dto.SearchResponseDto;

import lombok.RequiredArgsConstructor;
import reactor.core.publisher.Mono;

@Service
@RequiredArgsConstructor
public class SearchService {

	@Value("${opensearch.username}")
	private String username;

	@Value("${opensearch.password}")
	private String password;

	@Value("${opensearch.base-url}")
	private String baseUrl;

	private final WebClientConfig webClientConfig;

	public Mono<SearchResponseDto> search(String query) {
		return webClientConfig.webClient().post()
				.uri(baseUrl + "/product/_search")
				.header("Authorization", "Basic " + Base64.getEncoder().encodeToString((username + ":" + password).getBytes()))
				.header("Content-Type", "application/json")
				.bodyValue(query)
				.retrieve()
				.bodyToMono(SearchResponseDto.class);
	}
}