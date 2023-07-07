package org.swmaestro.repl.gifthub.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;

@ExtendWith(MockitoExtension.class)
class JwtUtilTest {
	@Mock
	private JwtUtil jwtUtil;

	@Test
	@DisplayName("generateToken test")
	void genderateToken() {
		String username = "jinwoolee";

		jwtUtil.generateToken(username);

		verify(jwtUtil, times(1)).generateToken(username);
	}
}