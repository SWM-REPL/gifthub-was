package org.swmaestro.repl.gifthub.auth.service;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.RefreshToken;
import org.swmaestro.repl.gifthub.auth.repository.RefreshTokenRepository;
import org.swmaestro.repl.gifthub.util.JwtProvider;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public void storeRefreshToken(TokenDto tokenDto, String username) {
		RefreshToken refreshToken = RefreshToken.builder()
				.refreshToken(tokenDto.getRefreshToken())
				.username(username)
				.build();

		if (refreshTokenRepository.findByUsername(username).isPresent()) {
			refreshTokenRepository.deleteByUsername(username);
		}
		refreshTokenRepository.save(refreshToken);
	}

	public String createNewAccessTokenByValidateRefreshToken(String refreshToken) {
		if (jwtProvider.validateToken(refreshToken)) {
			refreshToken = refreshToken.substring(7);
			return jwtProvider.reissueAccessToken(refreshToken);
		}
		return null;
	}

	public String createNewRefreshTokenByValidateRefreshToken(String refreshToken) {
		if (jwtProvider.validateToken(refreshToken)) {
			refreshToken = refreshToken.substring(7);
			return jwtProvider.generateRefreshToken(jwtProvider.getUsername(refreshToken));
		}
		return null;
	}
}
