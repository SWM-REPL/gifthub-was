package org.swmaestro.repl.gifthub.auth.service;

import org.bouncycastle.asn1.pkcs.PrivateKeyInfo;
import org.bouncycastle.openssl.PEMParser;
import org.bouncycastle.openssl.jcajce.JcaPEMKeyConverter;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

import java.io.*;
import java.security.PrivateKey;

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

	public AppleService(MemberService memberService,
	                    MemberRepository memberRepository,
	                    @Value("${apple.key-id}") String keyId,
	                    @Value("${apple.key-id-path}") String keyIdPath,
	                    @Value("${apple.key}") String key,
	                    @Value("${apple.team-id}") String teamId,
	                    @Value("${apple.authorization-uri}") String authorizationUri) {
		this.memberService = memberService;
		this.memberRepository = memberRepository;
		this.keyId = keyId;
		this.keyIdPath = keyIdPath;
		this.key = key;
		this.teamId = teamId;
		this.authorizationUri = authorizationUri;
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
}
