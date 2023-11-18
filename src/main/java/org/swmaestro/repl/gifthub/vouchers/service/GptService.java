package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.nio.charset.StandardCharsets;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.config.WebClientConfig;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherAutoSaveRequestDto;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.node.ArrayNode;
import com.fasterxml.jackson.databind.node.ObjectNode;

import reactor.core.publisher.Mono;

@Service
public class GptService {
	@Value("${openai.api-url}")
	private String apiUrl;
	@Value("${openai.api-key}")
	private String apiKey;
	private String prompt;

	private ObjectMapper objectMapper;
	private final WebClientConfig webClientConfig;

	public GptService(@Value("/gpt/question.txt") String promptPath, WebClientConfig webClientConfig, ObjectMapper objectMapper) throws IOException {
		this.prompt = loadQuestionFromFile(promptPath);
		this.webClientConfig = webClientConfig;
		this.objectMapper = objectMapper;
	}

	public Mono<GptResponseDto> getGptResponse(VoucherAutoSaveRequestDto voucherAutoSaveRequestDto) {
		String question = prompt;
		String content = voucherAutoSaveRequestDto.concatenateTexts();

		String prompt = content + question;
		ObjectNode requestBody = objectMapper.createObjectNode();
		requestBody.put("model", "gpt-3.5-turbo");
		ArrayNode messages = requestBody.putArray("messages");
		ObjectNode message = messages.addObject();
		message.put("role", "assistant");
		message.put("content", prompt);

		return webClientConfig.webClient().post()
				.uri(apiUrl)
				.header("Authorization", "Bearer " + apiKey)
				.header("Content-Type", "application/json")
				.body(Mono.just(requestBody), ObjectNode.class)
				.retrieve()
				.bodyToMono(GptResponseDto.class)
				.retry(2);
	}

	public String loadQuestionFromFile(String filePath) throws IOException {
		try (InputStream inputStream = getClass().getResourceAsStream(filePath)) {
			if (inputStream == null) {
				throw new FileNotFoundException("File not found: " + filePath);
			}
			return new String(inputStream.readAllBytes(), StandardCharsets.UTF_8);
		}
	}
}