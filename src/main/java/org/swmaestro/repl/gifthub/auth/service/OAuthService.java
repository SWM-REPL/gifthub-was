package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
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

	public OAuth create(Member member, OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.create(member, oAuthUserInfoDto);
			case KAKAO -> kakaoService.create(member, oAuthUserInfoDto);
			case GOOGLE -> googleService.create(member, oAuthUserInfoDto);
			case APPLE -> appleService.create(member, oAuthUserInfoDto);
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

	public boolean isExists(Member member, OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService.isExists(member);
			case KAKAO -> kakaoService.isExists(member);
			case GOOGLE -> googleService.isExists(member);
			case APPLE -> appleService.isExists(member);
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
