package org.swmaestro.repl.gifthub.auth.service;

import lombok.RequiredArgsConstructor;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.exception.ErrorCode;
import org.swmaestro.repl.gifthub.util.JwtProvider;

import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Service
@RequiredArgsConstructor
public class MemberServiceImpl implements MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final JwtProvider jwtProvider;
	private final RefreshTokenService refreshTokenService;

	public Member passwordEncryption(Member member) {
		return Member.builder()
				.username(member.getUsername())
				.password(passwordEncoder.encode(member.getPassword()))
				.nickname(member.getNickname())
				.build();
	}

	@Override
	public TokenDto create(SignUpDto signUpDTO) {
		if (isDuplicateUsername(signUpDTO.getUsername())) {
			throw new BusinessException("이미 존재하는 아이디입니다.", ErrorCode.EXIST_RESOURCE);
		}
		if (!isValidatePassword(signUpDTO.getPassword())) {
			throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", ErrorCode.INVALID_INPUT_VALUE);
		}

		Member member = convertSignUpDTOtoMember(signUpDTO);
		Member encodedMember = passwordEncryption(member);

		memberRepository.save(encodedMember);

		String accessToken = jwtProvider.generateToken(encodedMember.getUsername());
		String refreshToken = jwtProvider.generateRefreshToken(encodedMember.getUsername());

		TokenDto tokenDto = TokenDto.builder()
				.accessToken(accessToken)
				.refreshToken(refreshToken)
				.build();

		refreshTokenService.storeRefreshToken(tokenDto, encodedMember.getUsername());

		return tokenDto;
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

	public boolean isValidatePassword(String password) {
		String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=_\\-!]).{8,64}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);

		return matcher.matches();
	}

	@Override
	public Member read(String username) {
		Member member = memberRepository.findByUsername(username);
		if (member == null) {
			return null;
		}
		return member;
	}

	@Override
	public int count() {
		return (int) memberRepository.count();
	}

	@Override
	public List<Member> list() {
		return memberRepository.findAll();
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