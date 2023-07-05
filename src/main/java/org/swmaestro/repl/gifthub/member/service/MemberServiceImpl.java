package org.swmaestro.repl.gifthub.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {
	private final SpringDataJpaMemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;

	@Autowired
	public MemberServiceImpl(SpringDataJpaMemberRepository memberRepository, PasswordEncoder passwordEncoder) {
		this.memberRepository = memberRepository;
		this.passwordEncoder = passwordEncoder;
	}

	public Member passwordEncryption(Member member) {
		member.setPassword(passwordEncoder.encode(member.getPassword()));
		return member;
	}

	@Override
	public Long create(Member member) {
		Member encodedMember = passwordEncryption(member);
		memberRepository.save(encodedMember);
		return encodedMember.getId();
	}

	@Override
	public Member read(Long id) {
		return null;
	}

	@Override
	public int count() {
		return 0;
	}

	@Override
	public List<Member> list() {
		return null;
	}

	@Override
	public Long update(Long id, Member member) {
		return null;
	}

	@Override
	public Long delete(Long id) {
		return null;
	}
}
