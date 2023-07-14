package org.swmaestro.repl.gifthub.config;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.security.access.AccessDeniedException;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.AuthenticationEntryPoint;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.access.AccessDeniedHandler;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;
import org.swmaestro.repl.gifthub.filter.JwtAuthenticationFilter;

import java.io.IOException;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable())
			.authorizeHttpRequests(authorizeHttpRequests ->
				authorizeHttpRequests.requestMatchers("/auth/sign-up", "/auth/sign-in", "/auth/kakao/**", "auth/naver-login",
						"/swagger-resources/**", "/swagger-ui/**", "/v3/api-docs/**", "/webjars/**", "/error").permitAll()
					.anyRequest().authenticated())
			.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
			.exceptionHandling(exceptionHandling -> exceptionHandling
				.accessDeniedHandler(new AccessDeniedHandler() {
					@Override
					public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws IOException, ServletException, IOException {
						// 권한 문제가 발생했을 때 이 부분을 호출한다.
						response.setStatus(403);
						response.setCharacterEncoding("utf-8");
						response.setContentType("text/html; charset=UTF-8");
						response.getWriter().write("권한이 없습니다.");
					}
				})
				.authenticationEntryPoint(new AuthenticationEntryPoint() {
					@Override
					public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws IOException, ServletException {
						// 인증문제가 발생했을 때 이 부분을 호출한다.
						response.setStatus(401);
						response.setCharacterEncoding("utf-8");
						response.setContentType("text/html; charset=UTF-8");
						response.getWriter().write("인증되지 않은 사용자입니다.");
					}
				})
			);

		return httpSecurity.build();
	}


	@Bean
	public PasswordEncoder passwordEncoder() {
		return new BCryptPasswordEncoder();
	}
}
