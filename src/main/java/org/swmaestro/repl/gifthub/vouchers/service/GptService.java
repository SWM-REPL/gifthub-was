package org.swmaestro.repl.gifthub.vouchers.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.OCRDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service
public class GptService {
	private final WebClient gptClient;
	@Value("${openai.api-url}")
	private String apiUrl;
	@Value("${openai.api-key}")
	private String apiKey;

	@Autowired
	private ObjectMapper objectMapper;

	public GptService(WebClient.Builder webClientBuilder) {
		this.gptClient = webClientBuilder.build();
	}

	public Mono<GptResponseDto> getGptResponse(OCRDto ocrDto) {
		String question = "Please categorize them into 4 categories: brand name, product name, expiration date, and barcode number.\n"
				+ "But please keep this format and return it based on Korean.\n"
				+ " And Return the expiration date in this format, where year is a 4-digit number and month and day are 2-digit numbers.\n"
				+ " And the barcode number has 12 digits. Remove any hyphens or spaces and return it as 12 consecutive digits. \n"
				+ "If you can't categorize it, return an empty value in the JSON structure below."
				+ "\"year-month-day\"\n"
				+ "{\"brand_name\" : \n"
				+ "\"product_name\" :\n"
				+ "\"expires_at\" : \n"
				+ "\"barcode\":  }";

		String content = ocrDto.concatenateTexts();
		String prompt = question + content;
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("model", "gpt-3.5-turbo");
		ArrayNode messages = requestBody.putArray("messages");
		ObjectNode message = messages.addObject();
		message.put("role", "assistant");
		message.put("content", prompt);

		return gptClient.post()
				.uri(apiUrl)
				.header("Authorization", "Bearer " + apiKey)
				.body(Mono.just(requestBody), ObjectNode.class)
				.retrieve()
				.bodyToMono(GptResponseDto.class);
	}
}