package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.Reader;
import java.io.StringReader;
import java.security.PrivateKey;
import java.security.interfaces.RSAPublicKey;
import java.text.ParseException;
import java.util.Base64;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import org.swmaestro.repl.gifthub.auth.dto.AppleDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.nimbusds.jose.JOSEException;
import com.nimbusds.jose.JWSVerifier;
import com.nimbusds.jose.crypto.RSASSAVerifier;
import com.nimbusds.jose.jwk.JWK;
import com.nimbusds.jose.jwk.RSAKey;
import com.nimbusds.jwt.SignedJWT;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
@PropertySource("classpath:application.yml")
public class AppleService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	@Value("${apple.key-id}")
	private String keyId;
	@Value("${apple.key-id-path}")
	private String keyIdPath;
	@Value("${apple.key}")
	private String key;
	@Value("${apple.team-id}")
	private String teamId;
	@Value("${apple.base-url}")
	private String baseUrl;
	private final JwtProvider jwtProvider;

	public String readKeyPath() throws IOException {
		InputStream inputStream = this.getClass().getClassLoader().getResourceAsStream(keyIdPath);
		BufferedReader bufferedReader = new BufferedReader(new InputStreamReader(inputStream));
		String readLine = null;
		StringBuilder stringBuilder = new StringBuilder();
		while ((readLine = bufferedReader.readLine()) != null) {
			stringBuilder.append(readLine);
			stringBuilder.append("\n");
		}
		return stringBuilder.toString();
	}

	public PrivateKey craetePrivateKey(String keyPath) throws IOException {
		Reader reader = new StringReader(keyPath);
		PEMParser pemParser = new PEMParser(reader);
		JcaPEMKeyConverter jcaPEMKeyConverter = new JcaPEMKeyConverter();
		PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo)pemParser.readObject();
		return jcaPEMKeyConverter.getPrivateKey(privateKeyInfo);
	}

	public String createClientSecretKey(PrivateKey privateKey) {
		Map<String, Object> headerParamsMap = new HashMap<>();
		headerParamsMap.put("kid", keyId);
		headerParamsMap.put("alg", "ES256");

		return Jwts
				.builder()
				.setHeaderParams(headerParamsMap)
				.setIssuer(teamId)
				.setIssuedAt(new Date(System.currentTimeMillis()))
				.setExpiration(new Date(System.currentTimeMillis() + 1000 * 30)) // 만료 시간 (30초)
				.setAudience(baseUrl)
				.setSubject(key)
				.signWith(SignatureAlgorithm.ES256, privateKey)
				.compact();
	}

	public String getIdToken(String clientSecretKey, String code) throws IOException {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
				.defaultHeader(HttpHeaders.CONTENT_TYPE, MediaType.APPLICATION_JSON_VALUE)
				.build();

		Map<String, Object> tokenResponse =
				webClient
						.post()
						.uri(uriBuilder -> uriBuilder
								.path("/auth/token")
								.queryParam("grant_type", "authorization_code")
								.queryParam("client_id", key)
								.queryParam("client_secret", clientSecretKey)
								.queryParam("code", code)
								.build())
						.retrieve()
						.bodyToMono(Map.class)
						.block();

		return (String)tokenResponse.get("id_token");
	}

	public AppleDto getUserInfo(String idToken) throws ParseException, JsonProcessingException, JOSEException {
		WebClient webClient = WebClient.builder()
				.baseUrl(baseUrl)
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

		SignedJWT signedJWT = SignedJWT.parse(idToken);
		for (Map<String, Object> key : keys) {
			RSAKey rsaKey = (RSAKey)JWK.parse(new ObjectMapper().writeValueAsString(key));
			RSAPublicKey rsaPublicKey = rsaKey.toRSAPublicKey();
			JWSVerifier jwsVerifier = new RSASSAVerifier(rsaPublicKey);

			// idToken을 암호화한 key인 경우
			if (signedJWT.verify(jwsVerifier)) {
				// jwt를 .으로 나눴을때 가운데에 있는 payload 확인
				String payload = idToken.split("[.]")[1];
				// public key로 idToken 복호화
				Map<String, Object> payloadMap = new ObjectMapper().readValue(new String(Base64.getDecoder().decode(payload)), Map.class);

				String id = payloadMap.get("sub").toString();
				// 사용자 이메일 정보 추출
				String email = payloadMap.get("email").toString();
				String name = payloadMap.containsKey("name") ? name = payloadMap.get("name").toString() : "이름없는 사용자";

				return AppleDto.builder()
						.id(id)
						.email(email)
						.nickname(name)
						.build();
			}
		}
		return null;
	}

	public Member signUp(AppleDto appleDto) {
		Member member = Member.builder()
				.username(appleDto.getEmail())
				.nickname(appleDto.getNickname())
				.build();

		if (!memberService.isDuplicateUsername(appleDto.getEmail())) {
			memberRepository.save(member);
		}

		return member;
	}

	public TokenDto signIn(AppleDto appleDto, Long userId) {
		return TokenDto.builder()
				.accessToken(jwtProvider.generateToken(appleDto.getEmail(), userId))
				.refreshToken(jwtProvider.generateRefreshToken(appleDto.getEmail(), userId))
				.build();
	}
}
