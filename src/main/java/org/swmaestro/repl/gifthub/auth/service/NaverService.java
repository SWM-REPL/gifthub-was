package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.config.NaverConfig;
import org.swmaestro.repl.gifthub.auth.dto.OAuth2UserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.repository.OAuthRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class NaverService implements OAuth2Service {
	private final NaverConfig naverConfig;
	private final JsonParser parser = new JsonParser();
	private final OAuthRepository oAuthRepository;

	@Override
	public OAuth2UserInfoDto getUserInfo(TokenDto tokenDto) {

		try {
			String accessToken = tokenDto.getAccessToken();

			URL url = new URL(naverConfig.getUserInfoUri());
			HttpURLConnection con = (HttpURLConnection)url.openConnection();
			con.setRequestMethod("GET");
			con.setRequestProperty("Authorization", "Bearer " + accessToken);

			int responseCode = con.getResponseCode();
			BufferedReader br;

			if (responseCode == 200) { // 정상 호출
				br = new BufferedReader(new InputStreamReader(con.getInputStream()));
			} else {  // 에러 발생
				br = new BufferedReader(new InputStreamReader(con.getErrorStream()));
			}

			String inputLine;
			StringBuffer response = new StringBuffer();
			while ((inputLine = br.readLine()) != null) {
				response.append(inputLine);
			}

			br.close();

			JsonElement responseElement = parser.parse(response.toString()).getAsJsonObject().get("response").getAsJsonObject();

			String id = getStringOrNull(responseElement, "id");
			String email = getStringOrNull(responseElement, "email");
			String nickname = getStringOrNull(responseElement, "nickname");

			return OAuth2UserInfoDto.builder()
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
	public OAuth save(Member member, OAuth2UserInfoDto oAuth2UserInfoDto) {
		if (isExists(member)) {
			throw new BusinessException("이미 존재하는 OAuth입니다.", StatusEnum.CONFLICT);
		}

		OAuth oAuth = OAuth.builder()
				.member(member)
				.platform(OAuthPlatform.NAVER)
				.platformId(oAuth2UserInfoDto.getId())
				.email(oAuth2UserInfoDto.getEmail())
				.nickname(oAuth2UserInfoDto.getNickname())
				.build();

		return oAuthRepository.save(oAuth);
	}

	@Override
	public boolean isExists(Member member) {
		return oAuthRepository.findByMemberAndPlatform(member, OAuthPlatform.NAVER).isPresent();
	}

	@Override
	public boolean isExists(OAuth2UserInfoDto oAuth2UserInfoDto) {
		return oAuthRepository.findByPlatformAndPlatformId(OAuthPlatform.NAVER, oAuth2UserInfoDto.getId()).isPresent();
	}

	private String getStringOrNull(JsonElement element, String fieldName) {
		JsonElement fieldElement = element.getAsJsonObject().get(fieldName);
		return !fieldElement.isJsonNull() ? fieldElement.getAsString() : null;
	}
}
