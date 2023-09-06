package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.DeviceTokenRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
	private final DeviceTokenRepository deviceTokenRepository;
	private final MemberService memberService;

	/*
	 * DeviceToken 저장 메서드
	 */
	public DeviceToken save(String username, String token) {
		Member member = memberService.read(username);
		DeviceToken deviceToken = DeviceToken.builder()
				.member(member)
				.token(token)
				.build();
		return deviceTokenRepository.save(deviceToken);
	}
}
