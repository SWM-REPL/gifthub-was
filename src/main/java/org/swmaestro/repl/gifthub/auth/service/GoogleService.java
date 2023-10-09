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
import org.swmaestro.repl.gifthub.auth.dto.GoogleDto;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
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
public class GoogleService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final RefreshTokenService refreshTokenService;
	private final JwtProvider jwtProvider;
	@Value("${google.user_info_uri}")
	private String userInfoUri;

	public GoogleDto getUserInfo(JwtTokenDto jwtTokenDto) {
		GoogleDto googleDto = null;

		try {
			URL url = new URL(userInfoUri);
			HttpURLConnection conn = (HttpURLConnection)url.openConnection();

			conn.setRequestMethod("GET");
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

			String id = element.getAsJsonObject().get("id").getAsString();
			String nickname = element.getAsJsonObject().get("name").getAsString();
			String email = element.getAsJsonObject().get("email").getAsString();

			br.close();
			googleDto = GoogleDto.builder()
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
		return googleDto;
	}

	public JwtTokenDto signIn(GoogleDto googleDto) {
		if (memberService.isDuplicateUsername(googleDto.getUsername())) {
			JwtTokenDto jwtTokenDto = signInWithExistingMember(googleDto);
			return jwtTokenDto;
		}
		Member member = convertGoogleDtotoMember(googleDto);

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

	public JwtTokenDto signInWithExistingMember(GoogleDto googleDto) {
		Member member = memberRepository.findByUsername(googleDto.getUsername());
		if (member == null) {
			throw new BusinessException("존재하지 않는 아이디입니다.", StatusEnum.BAD_REQUEST);
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

	public Member convertGoogleDtotoMember(GoogleDto googleDto) {
		return Member.builder()
				.nickname(googleDto.getNickname())
				.username(googleDto.getUsername())
				.build();
	}
}
