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
		switch (platform) {
			case NAVER:
				return naverService.getUserInfo(oAuthTokenDto);
			case KAKAO:
				return kakaoService.getUserInfo(oAuthTokenDto);
			case GOOGLE:
				return googleService.getUserInfo(oAuthTokenDto);
			case APPLE:
				return appleService.getUserInfo(oAuthTokenDto);
			default:
				throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}

	public OAuth create(Member member, OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		switch (platform) {
			case NAVER:
				return naverService.create(member, oAuthUserInfoDto);
			case KAKAO:
				return kakaoService.create(member, oAuthUserInfoDto);
			case GOOGLE:
				return googleService.create(member, oAuthUserInfoDto);
			case APPLE:
				return appleService.create(member, oAuthUserInfoDto);
			default:
				throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}

	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		switch (platform) {
			case NAVER:
				return naverService.read(oAuthUserInfoDto);
			case KAKAO:
				return kakaoService.read(oAuthUserInfoDto);
			case GOOGLE:
				return googleService.read(oAuthUserInfoDto);
			case APPLE:
				return googleService.read(oAuthUserInfoDto);
			default:
				throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}

	public boolean isExists(Member member, OAuthPlatform platform) {
		switch (platform) {
			case NAVER:
				return naverService.isExists(member);
			case KAKAO:
				return kakaoService.isExists(member);
			case GOOGLE:
				return googleService.isExists(member);
			case APPLE:
				return appleService.isExists(member);
			default:
				throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}

	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		switch (platform) {
			case NAVER:
				return naverService.isExists(oAuthUserInfoDto);
			case KAKAO:
				return kakaoService.isExists(oAuthUserInfoDto);
			case GOOGLE:
				return googleService.isExists(oAuthUserInfoDto);
			case APPLE:
				return appleService.isExists(oAuthUserInfoDto);
			default:
				throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}
}
