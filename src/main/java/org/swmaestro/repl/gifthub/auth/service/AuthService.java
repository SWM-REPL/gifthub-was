package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.LoginDto;

public interface AuthService {
	LoginDto verifyPassword(LoginDto loginDto);
}
