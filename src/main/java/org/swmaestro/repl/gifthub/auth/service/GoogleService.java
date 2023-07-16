package org.swmaestro.repl.gifthub.auth.service;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.ErrorCode;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

@Service
@PropertySource("classpath:application.yml")
public class GoogleService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	private final String clientId;
	private final String redirectUri;
	private final String clientSecret;

	private final String tokenUri;

	private final String userInfoUri;
	private final String authorizationUri;

	public GoogleService(MemberService memberService, MemberRepository memberRepository, RefreshTokenService refreshTokenService, JwtProvider jwtProvider,
	                     @Value("${google.client_id}") String clientId, @Value("${google.client_secret}") String clientSecret, @Value("${google.redirect_uri}") String redirectUri,
	                     @Value("${google.token_uri}") String tokenUri, @Value("${google.user_info_uri}") String userInfoUri, @Value("${google.authorization_uri}") String authorizationUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.refreshTokenService = refreshTokenService;
		this.jwtProvider = jwtProvider;
		this.clientId = clientId;
		this.redirectUri = redirectUri;
		this.clientSecret = clientSecret;
		this.tokenUri = tokenUri;
		this.userInfoUri = userInfoUri;
		this.authorizationUri = authorizationUri;
	}

	public String getAuthorizationUrl() {
		return UriComponentsBuilder
				.fromUriString(authorizationUri)
				.queryParam("client_id", clientId)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("response_type", "code")
				.queryParam("scope", "email profile")
				.build()
				.toString();
	}

	public TokenDto getToken(String code) {

		TokenDto tokenDto = null;

		try {
			URL url = new URL(tokenUri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();

			sb.append("grant_type=authorization_code");
			sb.append("&client_id=" + clientId);
			sb.append("&client_secret=" + clientSecret);
			sb.append("&redirect_uri=" + redirectUri);
			sb.append("&code=" + code);

			bw.write(sb.toString());
			bw.flush();

			int responseCode = conn.getResponseCode();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));

			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}

			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);

			String accessToken = element.getAsJsonObject().get("access_token").getAsString();

			br.close();
			bw.close();
			tokenDto = TokenDto.builder()
					.accessToken(accessToken)
					.build();

		} catch (ProtocolException e) {
			throw new BusinessException("잘못된 프로토콜을 사용하였습니다.", ErrorCode.INVALID_INPUT_VALUE);
		} catch (MalformedURLException e) {
			throw new BusinessException("잘못된 URL 형식을 사용하였습니다.", ErrorCode.INVALID_INPUT_VALUE);
		} catch (IOException e) {
			throw new BusinessException("HTTP 연결을 수행하는 동안 입출력 관련 오류가 발생하였습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
		}
		return tokenDto;
	}

	public GoogleDto getUserInfo(TokenDto tokenDto) {

		GoogleDto googleDto = null;

		try {
			URL url = new URL(userInfoUri);
			HttpURLConnection conn = (HttpURLConnection) url.openConnection();

			conn.setRequestMethod("GET");
			conn.setDoOutput(true);

			conn.setRequestProperty("Authorization", "Bearer " + tokenDto.getAccessToken());

			int responseCode = conn.getResponseCode();

			BufferedReader br = new BufferedReader(new InputStreamReader(conn.getInputStream()));
			String line = "";
			String result = "";

			while ((line = br.readLine()) != null) {
				result += line;
			}

			JsonParser parser = new JsonParser();
			JsonElement element = parser.parse(result);

			String nickname = element.getAsJsonObject().get("name").getAsString();
			String email = element.getAsJsonObject().get("email").getAsString();

			br.close();
			googleDto = GoogleDto.builder()
					.nickname(nickname)
					.username(email)
					.build();
		} catch (ProtocolException e) {
			throw new BusinessException("잘못된 프로토콜을 사용하였습니다.", ErrorCode.INVALID_INPUT_VALUE);
		} catch (MalformedURLException e) {
			throw new BusinessException("잘못된 URL 형식을 사용하였습니다.", ErrorCode.INVALID_INPUT_VALUE);
		} catch (IOException e) {
			throw new BusinessException("HTTP 연결을 수행하는 동안 입출력 관련 오류가 발생하였습니다.", ErrorCode.INTERNAL_SERVER_ERROR);
		}
		return googleDto;
	}

	public TokenDto signIn(GoogleDto googleDto) {
		if (memberService.isDuplicateUsername(googleDto.getUsername())) {
			TokenDto tokenDto = signInWithExistingMember(googleDto);
			return tokenDto;
		}
		Member member = convertGoogleDtotoMember(googleDto);

		memberRepository.save(member);

		String accessToken = jwtProvider.generateToken(member.getUsername());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	public TokenDto signInWithExistingMember(GoogleDto googleDto) {
		Member member = memberRepository.findByUsername(googleDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", ErrorCode.INVALID_INPUT_VALUE);
		}
		String accessToken = jwtProvider.generateToken(member.getUsername());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	public Member convertGoogleDtotoMember(GoogleDto googleDto) {
		return Member.builder()
				.nickname(googleDto.getNickname())
				.username(googleDto.getUsername())
				.build();
	}
}
