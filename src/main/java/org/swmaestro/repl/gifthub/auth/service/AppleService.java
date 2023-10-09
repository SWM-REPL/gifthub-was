package org.swmaestro.repl.gifthub.auth.service;

import java.io.IOException;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.List;
import java.util.Map;

import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.swmaestro.repl.gifthub.auth.config.AppleConfig;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.repository.OAuthRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.google.gson.JsonParser;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class AppleService implements OAuth2Service {
	private final AppleConfig appleConfig;
	private final JsonParser parser = new JsonParser();
	private final OAuthRepository oAuthRepository;

	@Override
	public OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto) {
		try {
			String token = oAuthTokenDto.getToken();

			WebClient webClient = WebClient.builder()
					.baseUrl(appleConfig.getUserInfoUri())
					.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
					.build();

			Map<String, Object> keyReponse =
					webClient
							.get()
							.uri(uriBuilder -> uriBuilder
									.path("/auth/keys")
									.build())
							.retrieve()
							.bodyToMono(Map.class)
							.block();

			List<Map<String, Object>> keys = (List<Map<String, Object>>)keyReponse.get("keys");

			SignedJWT signedJWT = SignedJWT.parse(token);
			for (Map<String, Object> key : keys) {
				RSAKey rsaKey = (RSAKey)JWK.parse(new ObjectMapper().writeValueAsString(key));
				RSAPublicKey rsaPublicKey = rsaKey.toRSAPublicKey();
				JWSVerifier jwsVerifier = new RSASSAVerifier(rsaPublicKey);

				// idToken을 암호화한 key인 경우
				if (signedJWT.verify(jwsVerifier)) {
					// jwt를 .으로 나눴을때 가운데에 있는 payload 확인
					String payload = token.split("[.]")[1];
					// public key로 idToken 복호화
					Map<String, Object> payloadMap = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(payload)), Map.class);

					String id = payloadMap.get("sub").toString();
					String email = payloadMap.containsKey("email") ? payloadMap.get("email").toString() : null;
					String name = payloadMap.containsKey("name") ? payloadMap.get("name").toString() : null;

					return OAuthUserInfoDto.builder()
							.id(id)
							.email(email)
							.nickname(name)
							.build();
				}
			}
		} catch (ParseException e) {
			throw new BusinessException("Token Parsing에 실패하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		} catch (IOException e) {
			throw new BusinessException("IO Exception이 발생하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		} catch (JOSEException e) {
			throw new RuntimeException(e);
		}

		return null;
	}

	@Override
	public OAuth create(Member member, OAuthUserInfoDto oAuthUserInfoDto) {
		if (isExists(member)) {
			throw new BusinessException("이미 연동된 계정이 존재하는 플랫폼입니다.", StatusEnum.CONFLICT);
		}

		if (isExists(oAuthUserInfoDto)) {
			throw new BusinessException("이미 다른 계정과 연동된 계정입니다.", StatusEnum.CONFLICT);
		}

		OAuth oAuth = OAuth.builder()
				.member(member)
				.platform(OAuthPlatform.APPLE)
				.platformId(oAuthUserInfoDto.getId())
				.email(oAuthUserInfoDto.getEmail())
				.nickname(oAuthUserInfoDto.getNickname())
				.build();

		return oAuthRepository.save(oAuth);
	}

	@Override
	public OAuth read(OAuthUserInfoDto oAuthUserInfoDto) {
		return oAuthRepository.findByPlatformAndPlatformId(OAuthPlatform.APPLE, oAuthUserInfoDto.getId())
				.orElseThrow(() -> new BusinessException("존재하지 않는 OAuth 계정입니다.", StatusEnum.NOT_FOUND));
	}

	@Override
	public boolean isExists(Member member) {
		return oAuthRepository.findByMemberAndPlatform(member, OAuthPlatform.APPLE).isPresent();
	}

	@Override
	public boolean isExists(OAuthUserInfoDto oAuthUserInfoDto) {
		return oAuthRepository.findByPlatformAndPlatformId(OAuthPlatform.APPLE, oAuthUserInfoDto.getId()).isPresent();
	}
}
