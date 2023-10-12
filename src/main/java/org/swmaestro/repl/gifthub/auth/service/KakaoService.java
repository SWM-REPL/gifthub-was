package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;
import java.time.LocalDateTime;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.config.KakaoConfig;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.OAuthRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class KakaoService implements OAuth2Service {
	private final KakaoConfig kakaoConfig;
	private final JsonParser parser = new JsonParser();
	private final OAuthRepository oAuthRepository;

	@Override
	public OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto) {
		try {
			String token = oAuthTokenDto.getToken();

			URL url = new URL(kakaoConfig.getUserInfoUri());

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			conn.setRequestProperty("Authorization", "Bearer " + token);

			int responseCode = conn.getResponseCode();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}

			JsonElement responseElement = parser.parse(result);
			String id = getStringOrNull(responseElement, "id");
			String email = getStringOrNull(responseElement.getAsJsonObject().get("kakao_account").getAsJsonObject().get("profile"), "email");
			String nickname = getStringOrNull(responseElement.getAsJsonObject().get("kakao_account"), "nickname");

			return OAuthUserInfoDto.builder()
					.id(id)
					.email(email)
					.nickname(nickname)
					.build();
		} catch (MalformedURLException e) {
			throw new BusinessException("잘못된 URL을 통해 요청하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		} catch (ProtocolException e) {
			throw new BusinessException("잘못된 Protocol을 통해 요청하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			throw new BusinessException("IO Exception이 발생하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
	}

	@Override
	public OAuth create(User user, OAuthUserInfoDto oAuthUserInfoDto) {
		if (isExists(user)) {
			throw new BusinessException("이미 연동된 계정이 존재하는 플랫폼입니다.", StatusEnum.CONFLICT);
		}

		if (isExists(oAuthUserInfoDto)) {
			throw new BusinessException("이미 다른 계정과 연동된 계정입니다.", StatusEnum.CONFLICT);
		}

		OAuth oAuth = OAuth.builder()
				.member(user)
				.platform(OAuthPlatform.KAKAO)
				.platformId(oAuthUserInfoDto.getId())
				.email(oAuthUserInfoDto.getEmail())
				.nickname(oAuthUserInfoDto.getNickname())
				.build();

		return oAuthRepository.save(oAuth);
	}

	@Override
	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto) {
		OAuth oAuth = oAuthRepository.findByPlatformAndPlatformId(OAuthPlatform.KAKAO, oAuthUserInfoDto.getId())
				.orElseThrow(() -> new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND));

		if (oAuth.getDeletedAt() != null) {
			throw new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND);
		} else {
			return oAuth;
		}
	}

	@Override
	public OAuth delete(User user) {
		OAuth oAuth = oAuthRepository.findByMemberAndPlatform(user, OAuthPlatform.KAKAO).orElseThrow(
				() -> new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND)
		);
		oAuth.setDeletedAt(LocalDateTime.now());
		return oAuthRepository.save(oAuth);
	}

	@Override
	public boolean isExists(User user) {
		return oAuthRepository.findByMemberAndPlatform(user, OAuthPlatform.KAKAO).isPresent();
	}

	@Override
	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto) {
		return oAuthRepository.findByPlatformAndPlatformId(OAuthPlatform.KAKAO, oAuthUserInfoDto.getId()).isPresent();
	}

	private String getStringOrNull(JsonElement element, String fieldName) {
		JsonElement fieldElement = element.getAsJsonObject().get(fieldName);
		return !fieldElement.isJsonNull() ? fieldElement.getAsString() : null;
	}
}
