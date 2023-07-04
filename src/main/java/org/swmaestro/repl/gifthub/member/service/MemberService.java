package org.swmaestro.repl.gifthub.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

@Service
public class MemberService {
	private final SpringDataJpaMemberRepository memberRepository;

	@Autowired
	public MemberService(SpringDataJpaMemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public void signUp(Member member) {
		memberRepository.save(member);
	}
}
