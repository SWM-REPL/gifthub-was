package org.swmaestro.repl.gifthub.auth.dto;

import lombok.Builder;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class OAuthUserInfoDto {
	private String id;
	private String email;
	private String nickname;
	private String Provider;

	@Builder
	public OAuthUserInfoDto(String id, String email, String nickname, String Provider) {
		this.id = id;
		this.email = email;
		this.nickname = nickname;
		this.Provider = Provider;
	}
}
