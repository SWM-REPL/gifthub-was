package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

@Service
@PropertySource("classpath:application.yml")
public class AppleService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final String keyId;
	private final String keyPath;
	private final String key;
	private final String teamId;
	private final String authorizationUri;

	public AppleService(MemberService memberService,
	                    MemberRepository memberRepository,
	                    @Value("${apple.key-id}") String keyId,
	                    @Value("${apple.key-path}") String keyPath,
	                    @Value("${apple.key}") String key,
	                    @Value("${apple.team-id}") String teamId,
	                    @Value("${apple.authorization-uri}") String authorizationUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.keyId = keyId;
		this.keyPath = keyPath;
		this.key = key;
		this.teamId = teamId;
		this.authorizationUri = authorizationUri;
	}
}
