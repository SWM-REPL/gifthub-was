package org.swmaestro.repl.gifthub.auth.service;

import io.jsonwebtoken.Jwts;
import io.jsonwebtoken.SignatureAlgorithm;
import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.springframework.web.util.UriComponentsBuilder;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

import java.io.*;
import java.security.PrivateKey;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

@Service
@PropertySource("classpath:application.yml")
public class AppleService {
	private final MemberService memberService;
	private final MemberRepository memberRepository;
	private final String keyId;
	private final String keyIdPath;
	private final String key;
	private final String teamId;
	private final String authorizationUri;
	private final String responseType;
	private final String responseMode;
	private final String scope;
	private final String clientId;
	private final String redirectUri;
	private final String baseUrl;

	public AppleService(MemberService memberService,
	                    MemberRepository memberRepository,
	                    @Value("${apple.client-id}") String clientId,
	                    @Value("${apple.key-id}") String keyId,
	                    @Value("${apple.key-id-path}") String keyIdPath,
	                    @Value("${apple.key}") String key,
	                    @Value("${apple.team-id}") String teamId,
	                    @Value("${apple.authorization-uri}") String authorizationUri,
	                    @Value("${apple.response-type}") String responseType,
	                    @Value("${apple.response-mode}") String responseMode,
	                    @Value("${apple.scope}") String scope,
	                    @Value("${apple.redirect-uri}") String redirectUri,
	                    @Value("${apple.base-url}") String baseUrl) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.keyId = keyId;
		this.keyIdPath = keyIdPath;
		this.key = key;
		this.teamId = teamId;
		this.authorizationUri = authorizationUri;
		this.responseType = responseType;
		this.responseMode = responseMode;
		this.scope = scope;
		this.clientId = clientId;
		this.redirectUri = redirectUri;
		this.baseUrl = baseUrl;
	}

	public String getAuthorizationUrl() {
		return UriComponentsBuilder
			.fromUriString(authorizationUri)
			.queryParam("response_type", responseType)
			.queryParam("response_mode", responseMode)
			.queryParam("scope", scope)
			.queryParam("client_id", clientId)
			.queryParam("redirect_uri", redirectUri)
			.build().toString();
	}

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
		PrivateKeyInfo privateKeyInfo = (PrivateKeyInfo) pemParser.readObject();
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
}
