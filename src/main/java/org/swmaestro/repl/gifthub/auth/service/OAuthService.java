package org.swmaestro.repl.gifthub.auth.service;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.OAuthRepository;
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
	private final OAuthRepository oAuthRepository;

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

	/**
	 * 해당 유저의 전체 oauth 정보를 삭제하는 메서드
	 * @param user
	 * @return
	 */
	public List<OAuth> delete(User user) {
		List<OAuth> oAuths = oAuthRepository.findAllByUser(user);
		for (OAuth oAuth : oAuths) {
			oAuth.setDeletedAt(LocalDateTime.now());
			oAuthRepository.save(oAuth);
		}
		return oAuths;
	}

	public List<OAuthUserInfoDto> list(User user) {
		List<OAuth> oAuths = oAuthRepository.findAllByUserAndDeletedAtIsNull(user);
		if (oAuths == null)
			return null;
		List<OAuthUserInfoDto> oAuthUserInfoDtos = new ArrayList<>();
		for (OAuth oAuth : oAuths) {
			oAuthUserInfoDtos.add(mapToDto(oAuth));
		}
		return oAuthUserInfoDtos;
	}

	public OAuthUserInfoDto mapToDto(OAuth oAuth) {
		return OAuthUserInfoDto.builder()
				.id(oAuth.getPlatformId())
				.email(oAuth.getEmail())
				.nickname(oAuth.getNickname())
				.Provider(oAuth.getPlatform().toString())
				.build();
	}
}
