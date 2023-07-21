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
public class SignUpDto implements Serializable {
	@NotNull
	@Size(min = 4, max = 60)
	private String username;

	@NotNull
	@Size(min = 8, max = 64)
	private String password;

	@NotNull
	@Size(min = 2, max = 12)
	private String nickname;

	@Builder
	public SignUpDto(String username, String password, String nickname) {
		this.username = username;
		this.password = password;
		this.nickname = nickname;
	}
}
