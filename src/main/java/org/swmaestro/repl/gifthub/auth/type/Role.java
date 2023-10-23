package org.swmaestro.repl.gifthub.auth.type;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum Role {
	ADMIN("ROLE_ADMIN,ROLE_USER,ROLE_TEMPORARY"),
	USER("ROLE_USER,ROLE_TEMPORARY"),
	ANONYMOUS("ROLE_ANONYMOUS");

	private String value;
}
