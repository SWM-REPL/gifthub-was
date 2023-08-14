package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.dto.NaverDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

@Service
@PropertySource("classpath:application.yml")
public class NaverService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final String clientId;
	private final String state;
	private final String responseType;
	private final String authorizationUri;
	private final String redirectUri;
	private final String clientSecret;
	private final String tokenUri;
	private final String userInfoUri;
	private final JsonParser parser = new JsonParser();

	public NaverService(MemberService memberService,
			MemberRepository memberRepository,
			@Value("${naver.client-id}") String clientId,
			@Value("${naver.state}") String state,
			@Value("${naver.response-type}") String responseType,
			@Value("${naver.authorization-uri}") String authorizationUri,
			@Value("${naver.redirect-uri}") String redirectUri,
			@Value("${naver.client-secret}") String clientSecret,
			@Value("${naver.token-uri}") String tokenUri,
			@Value("${naver.user-info-uri}") String userInfoUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.clientId = clientId;
		this.state = state;
		this.responseType = responseType;
		this.authorizationUri = authorizationUri;
		this.redirectUri = redirectUri;
		this.clientSecret = clientSecret;
		this.tokenUri = tokenUri;
		this.userInfoUri = userInfoUri;
	}

	public String getAuthorizationUrl() {
		return UriComponentsBuilder
				.fromUriString(authorizationUri)
				.queryParam("client_id", clientId)
				.queryParam("response_type", responseType)
				.queryParam("redirect_uri", redirectUri)
				.queryParam("state", state)
				.build().toString();
	}

	public TokenDto getNaverToken(String type, String code) throws IOException {
		URL url = new URL(UriComponentsBuilder
				.fromUriString(tokenUri)
				.queryParam("grant_type", "authorization_code")
				.queryParam("client_id", clientId)
				.queryParam("client_secret", clientSecret)
				.queryParam("code", code)
				.build()
				.toString());

		HttpURLConnection con = (HttpURLConnection)url.openConnection();
		con.setRequestMethod("GET");

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

		JsonElement element = parser.parse(response.toString());

		return TokenDto.builder()
				.accessToken(element.getAsJsonObject().get("access_token").getAsString())
				.refreshToken(element.getAsJsonObject().get("refresh_token").getAsString())
				.build();
	}

	public NaverDto getUserInfo(TokenDto token) throws IOException {
		String accessToken = token.getAccessToken();

		URL url = new URL(userInfoUri);
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

		JsonElement element = parser.parse(response.toString());

		return NaverDto.builder()
				.id(element.getAsJsonObject().get("response").getAsJsonObject().get("id").getAsString())
				.email(element.getAsJsonObject().get("response").getAsJsonObject().get("email").getAsString())
				.nickname(element.getAsJsonObject().get("response").getAsJsonObject().get("nickname").getAsString())
				.build();
	}

	public Member signUp(NaverDto naverDto) {
		SignUpDto signUpDto = SignUpDto.builder()
				.username(naverDto.getEmail())
				.nickname(naverDto.getNickname())
				.password(naverDto.getId())
				.build();

		if (!memberService.isDuplicateUsername(naverDto.getEmail())) {
			memberService.create(signUpDto);
		}

		return memberService.read(naverDto.getEmail());
	}
}
