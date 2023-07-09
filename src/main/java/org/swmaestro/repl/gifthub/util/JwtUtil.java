package org.swmaestro.repl.gifthub.util;

import io.jsonwebtoken.Claims;
import io.jsonwebtoken.Jws;
import io.jsonwebtoken.Jwts;
import jakarta.servlet.http.HttpServletRequest;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.PropertySource;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.swmaestro.repl.gifthub.auth.service.JpaUserDetailsService;

import java.util.Date;

@Component
@PropertySource("classpath:application.yml")
public class JwtUtil {
	private final String secretKey;
	private final long expiration;
	private final String issuer;
	private final JpaUserDetailsService userDetailsService;

	public JwtUtil(@Value("${jwt.secret-key}") String secretKey, @Value("${jwt.expiration-time}") long expiration, @Value("${issuer}") String issuer, JpaUserDetailsService userDetailsService) {
		this.secretKey = secretKey;
		this.expiration = expiration;
		this.issuer = issuer;
		this.userDetailsService = userDetailsService;
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
		try {
			// Bearer 검증
			if (!token.substring(0, "BEARER ".length()).equalsIgnoreCase("BEARER ")) {
				return false;
			} else {
				token = token.split(" ")[1].trim();
			}
			Jws<Claims> claims = Jwts.parserBuilder().setSigningKey(secretKey.getBytes()).build().parseClaimsJws(token);
			// 만료되었을 시 false
			return !claims.getBody().getExpiration().before(new Date());
		} catch (Exception e) {
			return false;
		}
	}

	/**
	 * HTTP Header에서 JWT 토큰을 가져오는 메소드
	 *
	 * @param request
	 * @return JWT 토큰
	 */
	public String resolveToken(HttpServletRequest request) {
		return request.getHeader("Authorization");
	}

	/**
	 * JWT 토큰에서 인증 정보를 가져오는 메소드
	 *
	 * @param token
	 * @return 인증 정보
	 */
	public Authentication getAuthentication(String token) {
		UserDetails userDetails = userDetailsService.loadUserByUsername(this.getUsername(token));
		return new UsernamePasswordAuthenticationToken(userDetails, "", userDetails.getAuthorities());
	}

	/**
	 * JWT 토큰에서 username을 가져오는 메소드
	 *
	 * @param token
	 * @return username
	 */
	public String getUsername(String token) {
		return Jwts.parserBuilder()
			.setSigningKey(secretKey.getBytes())
			.build()
			.parseClaimsJws(token)
			.getBody()
			.getSubject();
	}
}
