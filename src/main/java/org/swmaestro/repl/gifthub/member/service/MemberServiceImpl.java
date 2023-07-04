package org.swmaestro.repl.gifthub.member.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.member.entity.Member;
import org.swmaestro.repl.gifthub.member.repository.SpringDataJpaMemberRepository;

import java.util.List;

@Service
public class MemberServiceImpl implements MemberService {
	private final SpringDataJpaMemberRepository memberRepository;

	@Autowired
	public MemberServiceImpl(SpringDataJpaMemberRepository memberRepository) {
		this.memberRepository = memberRepository;
	}

	public void signUp(Member member) {
		memberRepository.save(member);
	}
	
	@Override
	public Long create() {
		return null;
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
