package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.repository.OAuthRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class OAuthService {
	private final OAuthRepository oAuthRepository;

	public void save(Member member, OAuthPlatform platform, String platformId) {
		if (isExists(member, platform)) {
			return;
		}

		OAuth OAuthInfo = OAuth.builder()
				.member(member)
				.platform(platform)
				.platformId(platformId)
				.build();

		oAuthRepository.save(OAuthInfo);
	}

	public boolean isExists(Member member, OAuthPlatform platform) {
		return oAuthRepository.findByMemberAndPlatform(member, platform).isPresent();
	}
}
