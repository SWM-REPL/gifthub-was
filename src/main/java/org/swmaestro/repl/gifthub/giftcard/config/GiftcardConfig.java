package org.swmaestro.repl.gifthub.giftcard.config;

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

import lombok.Getter;
import lombok.Setter;

@Component
@ConfigurationProperties(prefix = "giftcard")
@Getter
@Setter
public class GiftcardConfig {
	private int effectiveDay;
}
