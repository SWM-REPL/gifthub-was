package org.swmaestro.repl.gifthub.filter;

import java.io.IOException;

import org.springframework.core.annotation.Order;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Component;
import org.springframework.web.filter.OncePerRequestFilter;
import org.swmaestro.repl.gifthub.util.ErrorMessage;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import com.fasterxml.jackson.databind.ObjectMapper;

import jakarta.servlet.FilterChain;
import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;

@Order(0)
@Component
@RequiredArgsConstructor
public class JwtAuthenticationFilter extends OncePerRequestFilter {
	private final JwtProvider jwtProvider;
	private final ObjectMapper objectMapper = new ObjectMapper();

	@Override
	protected void doFilterInternal(HttpServletRequest request, HttpServletResponse response, FilterChain filterChain) throws ServletException, IOException,
			UsernameNotFoundException {
		String token = jwtProvider.resolveToken(request);
		if (token != null && jwtProvider.validateToken(token)) {
			token = token.substring(7);
			try {
				Authentication auth = jwtProvider.getAuthentication(token);
				SecurityContextHolder.getContext().setAuthentication(auth);
			} catch (UsernameNotFoundException e) {
				e.printStackTrace();
				response.setStatus(401);
				response.setCharacterEncoding("utf-8");
				response.setContentType("application/json");
				response.getWriter().write(objectMapper.writeValueAsString(
						ErrorMessage.builder()
								.status(StatusEnum.UNAUTHORIZED.statusCode)
								.path(request.getRequestURI())
								.error("탈퇴한 회원입니다.")
								.build()));
				return;
			}
		}
		filterChain.doFilter(request, response);
	}
}