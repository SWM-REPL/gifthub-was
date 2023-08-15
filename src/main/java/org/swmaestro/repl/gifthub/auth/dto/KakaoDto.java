package org.swmaestro.repl.gifthub.auth.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class KakaoDto {
	private String id;

	@NotNull
	@Size(min = 4, max = 60)
	private String username;

	@NotNull
	@Size(min = 2, max = 12)
	private String nickname;
}
