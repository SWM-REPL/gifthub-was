package org.swmaestro.repl.gifthub.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

public class MemberService {
	private SpringDataJpaMemberRepository memberRepository;

	@Autowired
	public MemberService(SpringDataJpaMemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public void signUp() {

	}
}
