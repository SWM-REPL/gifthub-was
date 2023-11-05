package org.swmaestro.repl.gifthub.auth.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "oauth2.apple")
@Getter
@Setter
public class AppleConfig {
	private String userInfoUri;
}