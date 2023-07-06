package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;

public interface AuthService {
    SignUpDto verifyPassword(SignUpDto signUpDto);
}
