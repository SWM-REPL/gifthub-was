package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.LoginDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;

public interface AuthService {
	TokenDto verifyPassword(LoginDto loginDto);
}
