package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.BufferedWriter;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStreamWriter;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Service
@PropertySource("classpath:application.yml")
public class KakaoService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final String clientId;
	private final String redirectUri;
	private final JwtProvider jwtProvider;
	private final String authorizationUri;
	private final String tokenUri;
	private final String userInfoUri;

	public KakaoService(MemberService memberService, MemberRepository memberRepository, RefreshTokenService refreshTokenService,
			JwtProvider jwtProvider, @Value("${kakao.client_id}") String clientId, @Value("${kakao.redirect_uri}") String redirectUri,
			@Value("${kakao.authorization_uri}") String authorizationUri, @Value("${kakao.user_info_uri}") String userInfoUri,
			@Value("${kakao.token_uri}") String tokenUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.refreshTokenService = refreshTokenService;
		this.jwtProvider = jwtProvider;
		this.clientId = clientId;
		this.redirectUri = redirectUri;
		this.authorizationUri = authorizationUri;
		this.userInfoUri = userInfoUri;
		this.tokenUri = tokenUri;
	}

	public String getAuthorizationUrl() {
		return UriComponentsBuilder
				.fromUriString(authorizationUri)
				.queryParam("client_id", clientId)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("response_type", "code")
				.build()
				.toString();
	}

	public TokenDto getToken(String code) throws MalformedURLException {

		TokenDto tokenDto = null;
		try {
			URL url = new URL(tokenUri);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			BufferedWriter bw = new BufferedWriter(new OutputStreamWriter(conn.getOutputStream()));
			StringBuilder sb = new StringBuilder();

			sb.append("grant_type=authorization_code");
			sb.append("&client_id=" + clientId);
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
			String refreshToken = element.getAsJsonObject().get("refresh_token").getAsString();

			br.close();
			bw.close();

			tokenDto = TokenDto.builder()
					.accessToken(accessToken)
					.refreshToken(refreshToken)
					.build();
		} catch (ProtocolException e) {
			throw new BusinessException("잘못된 프로토콜을 사용하였습니다.", StatusEnum.BAD_REQUEST);
		} catch (MalformedURLException e) {
			throw new BusinessException("잘못된 URL 형식을 사용하였습니다.", StatusEnum.BAD_REQUEST);
		} catch (IOException e) {
			throw new BusinessException("HTTP 연결을 수행하는 동안 입출력 관련 오류가 발생하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}

		return tokenDto;
	}

	public KakaoDto getUserInfo(TokenDto tokenDto) {

		KakaoDto kakaoDto = null;

		try {
			URL url = new URL(userInfoUri);

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
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

			String id = Integer.toString(element.getAsJsonObject().get("id").getAsInt());
			String nickname = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("profile").getAsJsonObject().get("nickname").getAsString();
			boolean hasEmail = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("has_email").getAsBoolean();
			String email = null;
			if (hasEmail) {
				email = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email").getAsString();
			}
			br.close();
			kakaoDto = KakaoDto.builder()
					.id(id)
					.nickname(nickname)
					.username(email)
					.build();
		} catch (ProtocolException e) {
			throw new BusinessException("잘못된 프로토콜을 사용하였습니다.", StatusEnum.BAD_REQUEST);
		} catch (MalformedURLException e) {
			throw new BusinessException("잘못된 URL 형식을 사용하였습니다.", StatusEnum.BAD_REQUEST);
		} catch (IOException e) {
			throw new BusinessException("HTTP 연결을 수행하는 동안 입출력 관련 오류가 발생하였습니다.", StatusEnum.INTERNAL_SERVER_ERROR);
		}
		return kakaoDto;
	}

	public TokenDto signIn(KakaoDto kakaoDto) {
		if (memberService.isDuplicateUsername(kakaoDto.getUsername())) {
			TokenDto tokenDto = signInWithExistingMember(kakaoDto);
			return tokenDto;
		}
		Member member = convertKakaoDtotoMember(kakaoDto);

		memberRepository.save(member);

		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	public TokenDto signInWithExistingMember(KakaoDto kakaoDto) {
		Member member = memberRepository.findByUsername(kakaoDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.NOT_FOUND);
		}
		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, member.getUsername());

		return tokenDto;
	}

	public Member convertKakaoDtotoMember(KakaoDto kakaoDto) {
		return Member.builder()
				.nickname(kakaoDto.getNickname())
				.username(kakaoDto.getUsername())
				.build();
	}

}
