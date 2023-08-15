package org.swmaestro.repl.gifthub.auth.type;

public enum OAuthPlatform {
	NAVER("NAVER", 0),
	KAKAO("KAKAO", 1),
	GOOGLE("GOOGLE", 2),
	APPLE("APPLE", 3);

	private String name;
	private int code;

	OAuthPlatform(String name, int code) {
		this.name = name;
		this.code = code;
	}
}
