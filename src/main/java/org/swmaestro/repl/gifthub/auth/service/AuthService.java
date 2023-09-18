package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.SignInDto;
import org.swmaestro.repl.gifthub.auth.dto.SignOutDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;

public interface AuthService {

	TokenDto signIn(SignInDto loginDto);

	void signOut(String username, SignOutDto signOutDto);
}
