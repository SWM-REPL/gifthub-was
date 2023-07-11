package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.SignInDto;

public interface AuthService {
	SignInDto signIn(SignInDto loginDto);
}
