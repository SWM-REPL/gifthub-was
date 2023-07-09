package org.swmaestro.repl.gifthub.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.entity.CustomUserDetails;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;

@Service
@RequiredArgsConstructor
public class JpaUserDetailsService implements UserDetailsService {
	private final MemberRepository memberRepository;

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		Member member = memberRepository.findByUsername(username);
		if (member != null) {
			return new CustomUserDetails(member);
		} else {
			return (UserDetails) new UsernameNotFoundException("User not found with username: " + username);
		}

	}
}
