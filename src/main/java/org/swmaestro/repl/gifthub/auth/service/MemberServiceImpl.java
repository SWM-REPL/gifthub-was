package org.swmaestro.repl.gifthub.auth.service;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.SpringDataJpaMemberRepository;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

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
		return Member.builder()
			.username(member.getUsername())
			.password(passwordEncoder.encode(member.getPassword()))
			.nickname(member.getNickname())
			.build();
	}

	@Override
	public Long create(SignUpDto signUpDTO) {
		if (isDuplicateUsername(signUpDTO.getUsername()) ||
			isDuplicateNickname(signUpDTO.getNickname()) ||
			!isValidatePassword(signUpDTO.getPassword())) {
			return -1L;
		}

		Member member = convertSignUpDTOtoMember(signUpDTO);
		Member encodedMember = passwordEncryption(member);

		memberRepository.save(encodedMember);
		return member.getId();
	}

	public Member convertSignUpDTOtoMember(SignUpDto signUpDTO) {
		return Member.builder()
			.username(signUpDTO.getUsername())
			.password(signUpDTO.getPassword())
			.nickname(signUpDTO.getNickname())
			.build();
	}

	public boolean isDuplicateUsername(String username) {
		return memberRepository.findByUsername(username) != null;
	}

	public boolean isDuplicateNickname(String nickname) {
		return memberRepository.findByNickname(nickname) != null;
	}

	public boolean isValidatePassword(String password) {
		String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=_\\-!]).{8,64}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);

		return matcher.matches();
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
