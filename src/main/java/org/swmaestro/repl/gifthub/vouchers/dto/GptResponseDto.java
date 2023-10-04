package org.swmaestro.repl.gifthub.vouchers.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class GptResponseDto {

	@JsonProperty("choices")
	private List<Choice> choices;

	@Getter
	public static class Choice {
		@JsonProperty("message")
		private Message message;

		@Getter
		public static class Message {
			@JsonProperty("content")
			private String content;
		}
	}
}