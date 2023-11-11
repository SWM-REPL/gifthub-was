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
import org.swmaestro.repl.gifthub.auth.config.GoogleConfig;
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
public class GoogleService implements OAuth2Service {
	private final GoogleConfig googleConfig;
	private final JsonParser parser = new JsonParser();
	private final OAuthRepository oAuthRepository;

	@Override
	public OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto) {
		try {
			String token = oAuthTokenDto.getToken();

			URL url = new URL(googleConfig.getUserInfoUri());
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("GET");
			conn.setDoOutput(true);

			conn.setRequestProperty("Authorization", "Bearer " + token);

			int responseCode = conn.getResponseCode();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}

			br.close();

			JsonElement responseElement = parser.parse(result).getAsJsonObject();

			String id = getStringOrNull(responseElement, "sub");
			String email = getStringOrNull(responseElement, "email");
			String nickname = getStringOrNull(responseElement, "name");

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
		} catch (Exception e) {
			throw new BusinessException("오류가 발생했습니다. 다시 시도해 주세요!", StatusEnum.INTERNAL_SERVER_ERROR);
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
				.user(user)
				.platform(OAuthPlatform.GOOGLE)
				.platformId(oAuthUserInfoDto.getId())
				.email(oAuthUserInfoDto.getEmail())
				.nickname(oAuthUserInfoDto.getNickname())
				.build();

		return oAuthRepository.save(oAuth);
	}

	@Override
	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto) {
		OAuth oAuth = oAuthRepository.findByPlatformAndPlatformIdAndDeletedAtIsNull(OAuthPlatform.GOOGLE, oAuthUserInfoDto.getId())
				.orElseThrow(() -> new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND));

		if (oAuth.isDeleted()) {
			throw new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND);
		} else {
			return oAuth;
		}
	}

	@Override
	public OAuth delete(User user) {
		OAuth oAuth = oAuthRepository.findByUserAndPlatform(user, OAuthPlatform.GOOGLE).orElseThrow(
				() -> new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND)
		);
		oAuth.setDeletedAt(LocalDateTime.now());
		return oAuthRepository.save(oAuth);
	}

	@Override
	public boolean isExists(User user) {
		if (!user.isEnabled()) {
			throw new BusinessException("탈퇴한 회원입니다.", StatusEnum.BAD_REQUEST);
		}
		return oAuthRepository.findByUserAndPlatform(user, OAuthPlatform.GOOGLE).isPresent();
	}

	@Override
	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto) {
		return oAuthRepository.findByPlatformAndPlatformIdAndDeletedAtIsNull(OAuthPlatform.GOOGLE, oAuthUserInfoDto.getId()).isPresent();
	}

	private String getStringOrNull(JsonElement element, String fieldName) {
		JsonElement fieldElement = element.getAsJsonObject().get(fieldName);
		return !fieldElement.isJsonNull() ? fieldElement.getAsString() : null;
	}
}
