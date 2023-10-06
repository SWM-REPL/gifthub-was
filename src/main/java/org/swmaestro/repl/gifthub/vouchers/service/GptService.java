package org.swmaestro.repl.gifthub.vouchers.service;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Paths;

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

	@Value("${openai.question-path}")
	private String promptPath;

	@Autowired
	private ObjectMapper objectMapper;

	public GptService(WebClient.Builder webClientBuilder) {
		this.gptClient = webClientBuilder.build();
	}

	public Mono<GptResponseDto> getGptResponse(OCRDto ocrDto) throws IOException {
		String question = loadQuestionFromFile(promptPath);
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

	public String loadQuestionFromFile(String filePath) throws IOException {
		return new String(Files.readAllBytes(Paths.get(filePath)), StandardCharsets.UTF_8);
	}
}