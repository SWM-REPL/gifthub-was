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
		return platformToService(platform).getUserInfo(oAuthTokenDto);
	}

	public OAuth create(User user, OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return platformToService(platform).create(user, oAuthUserInfoDto);
	}

	public OAuth delete(User user, OAuthPlatform platform) {
		return platformToService(platform).delete(user);
	}

	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return platformToService(platform).read(oAuthUserInfoDto);
	}

	public boolean isExists(User user, OAuthPlatform platform) {
		return platformToService(platform).isExists(user);
	}

	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto, OAuthPlatform platform) {
		return platformToService(platform).isExists(oAuthUserInfoDto);
	}

	public OAuth2Service platformToService(OAuthPlatform platform) {
		return switch (platform) {
			case NAVER -> naverService;
			case KAKAO -> kakaoService;
			case GOOGLE -> googleService;
			case APPLE -> appleService;
			default -> throw new BusinessException("지원하지 않는 OAuth 플랫폼입니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		};
	}
}
