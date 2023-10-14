package org.swmaestro.repl.gifthub.vouchers.dto;

import java.util.List;

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.PropertyNamingStrategy;
import com.fasterxml.jackson.databind.annotation.JsonNaming;

import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = lombok.AccessLevel.PROTECTED)
@JsonNaming(PropertyNamingStrategy.SnakeCaseStrategy.class)
public class SearchResponseDto {

	private Hits hits;

	@Getter
	public static class Hits {
		@JsonProperty("hits")
		private List<Hit> hitsList;
	}

	@Getter
	public static class Hit {
		@JsonProperty("_source")
		private Source source;
	}

	@Getter
	public static class Source {
		@JsonProperty("brand_name")
		private String brandName;

		@JsonProperty("brand_id")
		private Long brandId;

		@JsonProperty("product_name")
		private String productName;

		@JsonProperty("product_id")
		private Long productId;

	}

	@Builder
	public SearchResponseDto(Hits hits) {
		this.hits = hits;
	}
}