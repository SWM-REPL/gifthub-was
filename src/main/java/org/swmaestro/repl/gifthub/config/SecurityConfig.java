package org.swmaestro.repl.gifthub.config;

import java.io.IOException;

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
import org.swmaestro.repl.gifthub.util.ErrorMessage;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.fasterxml.jackson.databind.ObjectMapper;

import io.sentry.Sentry;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Configuration
@EnableWebSecurity(debug = true)
@RequiredArgsConstructor
public class SecurityConfig {
	private final JwtAuthenticationFilter jwtAuthenticationFilter;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Bean
	public SecurityFilterChain filterChain(HttpSecurity httpSecurity) throws Exception {
		httpSecurity.csrf(csrf -> csrf.disable())
				.authorizeHttpRequests(authorizeHttpRequests ->
						authorizeHttpRequests.requestMatchers(
										"/auth/sign-up",
										"/auth/sign-in",
										"/auth/sign-in/**",
										"/swagger-resources/**",
										"/swagger-ui/**",
										"/v3/api-docs/**",
										"/webjars/**",
										"/error").permitAll()
								.anyRequest().authenticated())
				.addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class)
				.exceptionHandling(exceptionHandling -> exceptionHandling
						.accessDeniedHandler(new AccessDeniedHandler() {
							@Override
							public void handle(HttpServletRequest request, HttpServletResponse response, AccessDeniedException accessDeniedException) throws
									IOException {
								// 권한 문제가 발생했을 때 이 부분을 호출한다.
								response.setStatus(403);
								response.setCharacterEncoding("utf-8");
								response.setContentType("application/json");
								response.getWriter().write(objectMapper.writeValueAsString(
										ErrorMessage.builder()
												.status(StatusEnum.FORBIDDEN.statusCode)
												.path(request.getRequestURI())
												.error("권한이 없습니다.")
												.build()));
							}
						})
						.authenticationEntryPoint(new AuthenticationEntryPoint() {
							@Override
							public void commence(HttpServletRequest request, HttpServletResponse response, AuthenticationException authException) throws
									IOException {
								// 인증문제가 발생했을 때 이 부분을 호출한다.
								response.setStatus(401);
								response.setCharacterEncoding("utf-8");
								response.setContentType("application/json");
								response.getWriter().write(objectMapper.writeValueAsString(
										ErrorMessage.builder()
												.status(StatusEnum.UNAUTHORIZED.statusCode)
												.path(request.getRequestURI())
												.error("인증되지 않은 사용자입니다.")
												.build()));
								Sentry.captureException(authException);
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
