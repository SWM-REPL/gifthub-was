package org.swmaestro.repl.gifthub.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;

@Component
@ConfigurationProperties(prefix = "oauth2.naver")
@Getter
public class NaverConfig {
	private String userInfoUri;
}
