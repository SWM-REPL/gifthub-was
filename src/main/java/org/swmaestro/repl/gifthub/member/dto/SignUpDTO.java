package org.swmaestro.repl.gifthub.member.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Builder
@Getter
@Setter
public class SignUpDTO {
	@NotNull
	@Size(min = 4, max = 60)
	private String username;

	@NotNull
	@Size(min = 8, max = 64)
	private String password;

	@NotNull
	@Size(min = 2, max = 12)
	private String nickname;
}
