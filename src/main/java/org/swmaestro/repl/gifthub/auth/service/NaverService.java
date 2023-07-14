package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

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


	public NaverService(MemberService memberService,
	                    MemberRepository memberRepository,
	                    @Value("${naver.client-id}") String clientId,
	                    @Value("${naver.state}") String state,
	                    @Value("${naver.response-type}") String responseType,
	                    @Value("${naver.authorization-uri}") String authorizationUri,
	                    @Value("${naver.redirect-uri}") String redirectUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.clientId = clientId;
		this.state = state;
		this.responseType = responseType;
		this.authorizationUri = authorizationUri;
		this.redirectUri = redirectUri;
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
}
