package org.swmaestro.repl.gifthub.giftcard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.security.crypto.encrypt.AesBytesEncryptor;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "giftcard")
@Getter
@Setter
public class GiftcardConfig {
	private int effectiveDay;
	private String secret;
	private String salt;

	@Bean
	public AesBytesEncryptor aesBytesEncryptor() {
		return new AesBytesEncryptor(secret, salt);
	}
}
