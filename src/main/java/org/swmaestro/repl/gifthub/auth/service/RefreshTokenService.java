package org.swmaestro.repl.gifthub.auth.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.entity.RefreshToken;
import org.swmaestro.repl.gifthub.auth.repository.RefreshTokenRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class RefreshTokenService {
	private final JwtProvider jwtProvider;
	private final RefreshTokenRepository refreshTokenRepository;

	@Transactional
	public void storeRefreshToken(JwtTokenDto jwtTokenDto, String username) {
		RefreshToken refreshToken = RefreshToken.builder()
				.token(jwtTokenDto.getRefreshToken())
				.username(username)
				.createdAt(jwtProvider.getIssuedAt(jwtTokenDto.getRefreshToken()))
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
			return jwtProvider.generateRefreshToken(jwtProvider.getUsername(refreshToken), jwtProvider.getUserId(refreshToken));
		}
		return null;
	}

	public void deleteRefreshToken(String username) {
		Optional<RefreshToken> refreshToken = refreshTokenRepository.findByUsername(username);
		if (refreshToken.isPresent()) {
			refreshTokenRepository.delete(refreshToken.get());
		} else {
			throw new BusinessException("존재하지 않는 사용자 입니다.", StatusEnum.UNAUTHORIZED);
		}
	}
}
