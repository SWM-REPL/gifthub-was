package org.swmaestro.repl.gifthub.auth.dto;

import java.io.Serializable;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Getter
@Setter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class SignInDto implements Serializable {
	@NotNull
	@Size(min = 4, max = 60)
	private String username;

	@NotNull
	@Size(min = 8, max = 64)
	private String password;

	@Builder
	public SignInDto(String username, String password) {
		this.username = username;
		this.password = password;
	}
}
