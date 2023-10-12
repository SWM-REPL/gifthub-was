package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
/**
 * OAuth 플랫폼에 따라서 OAuth2Service를 구현한 서비스를 호출하는 클래스
 */
public class OAuthService {
	private final NaverService naverService;
	private final KakaoService kakaoService;
	private final GoogleService googleService;
	private final AppleService appleService;

	public OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.getUserInfo(oAuthTokenDto);
			case KAKAO -> kakaoService.getUserInfo(oAuthTokenDto);
			case GOOGLE -> googleService.getUserInfo(oAuthTokenDto);
			case APPLE -> appleService.getUserInfo(oAuthTokenDto);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}

	public OAuth create(User user, OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.create(user, oAuthUserInfoDto);
			case KAKAO -> kakaoService.create(user, oAuthUserInfoDto);
			case GOOGLE -> googleService.create(user, oAuthUserInfoDto);
			case APPLE -> appleService.create(user, oAuthUserInfoDto);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}

	public OAuth delete(User user, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.delete(user);
			case KAKAO -> kakaoService.delete(user);
			case GOOGLE -> googleService.delete(user);
			case APPLE -> appleService.delete(user);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}

	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.read(oAuthUserInfoDto);
			case KAKAO -> kakaoService.read(oAuthUserInfoDto);
			case GOOGLE -> googleService.read(oAuthUserInfoDto);
			case APPLE -> appleService.read(oAuthUserInfoDto);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}

	public boolean isExists(User user, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.isExists(user);
			case KAKAO -> kakaoService.isExists(user);
			case GOOGLE -> googleService.isExists(user);
			case APPLE -> appleService.isExists(user);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}

	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.isExists(oAuthUserInfoDto);
			case KAKAO -> kakaoService.isExists(oAuthUserInfoDto);
			case GOOGLE -> googleService.isExists(oAuthUserInfoDto);
			case APPLE -> appleService.isExists(oAuthUserInfoDto);
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}
}
