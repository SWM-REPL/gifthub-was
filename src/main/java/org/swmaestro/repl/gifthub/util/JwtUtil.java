package org.swmaestro.repl.gifthub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.stereotype.Component;

@PropertySource("classpath:application.yml")
@Component
public class JwtUtil {
	private final String secretKey;
	private final long expiration;

	public JwtUtil(@Value("${jwt.secret-key}") String secretKey, @Value("${jwt.expiration-time}") long expiration) {
		this.secretKey = secretKey;
		this.expiration = expiration;
	}

	/**
	 * JWT 생성 메소드
	 *
	 * @param username
	 * @return JWT 토큰
	 */
	public String generateToken(String username) {
		return io.jsonwebtoken.Jwts.builder()
			.setSubject(username)
			.setIssuedAt(new java.util.Date(System.currentTimeMillis()))
			.setExpiration(new java.util.Date(System.currentTimeMillis() + expiration))
			.signWith(io.jsonwebtoken.SignatureAlgorithm.HS512, secretKey)
			.compact();
	}

	/**
	 * JWT 유효성 검사 메소드
	 *
	 * @param token
	 * @return 유효성 여부
	 */
	public boolean validateToken(String token) {
		Jws<Claims> claims = io.jsonwebtoken.Jwts.parser().setSigningKey(secretKey).parseClaimsJws(token);
		return !claims.getBody().getExpiration().before(new java.util.Date());
	}
}
