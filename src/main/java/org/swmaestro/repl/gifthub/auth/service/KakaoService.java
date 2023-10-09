package org.swmaestro.repl.gifthub.auth.service;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.ProtocolException;
import java.net.URL;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.KakaoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;

import lombok.RequiredArgsConstructor;

@Service
@PropertySource("classpath:application.yml")
@RequiredArgsConstructor
public class KakaoService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	@Value("${kakao.user_info_uri}")
	private String userInfoUri;

	public KakaoDto getUserInfo(JwtTokenDto jwtTokenDto) {

		KakaoDto kakaoDto = null;

		try {
			URL url = new URL(userInfoUri);

			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("POST");
			conn.setDoOutput(true);

			conn.setRequestProperty("Authorization", "Bearer " + jwtTokenDto.getAccessToken());

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

			JsonElement emailJsonElement = element.getAsJsonObject().get("kakao_account").getAsJsonObject().get("email");
			String email = "";
			if (emailJsonElement == null) {
				email = "";
			} else {
				email = emailJsonElement.getAsString();
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

	public JwtTokenDto signIn(KakaoDto kakaoDto) {
		if (memberService.isDuplicateUsername(kakaoDto.getUsername())) {
			JwtTokenDto jwtTokenDto = signInWithExistingMember(kakaoDto);
			return jwtTokenDto;
		}
		Member member = convertKakaoDtotoMember(kakaoDto);

		memberRepository.save(member);

		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(jwtTokenDto, member.getUsername());

		return jwtTokenDto;
	}

	public JwtTokenDto signInWithExistingMember(KakaoDto kakaoDto) {
		Member member = memberRepository.findByUsername(kakaoDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.NOT_FOUND);
		}
		String accessToken = jwtProvider.generateToken(member.getUsername(), member.getId());
		String refreshToken = jwtProvider.generateRefreshToken(member.getUsername(), member.getId());

		JwtTokenDto jwtTokenDto = JwtTokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(jwtTokenDto, member.getUsername());

		return jwtTokenDto;
	}

	public Member convertKakaoDtotoMember(KakaoDto kakaoDto) {
		return Member.builder()
				.nickname(kakaoDto.getNickname())
				.username(kakaoDto.getUsername())
				.build();
	}
}
