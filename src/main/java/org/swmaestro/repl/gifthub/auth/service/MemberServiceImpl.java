package org.swmaestro.repl.gifthub.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.SignUpDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

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
			throw new BusinessException("이미 존재하는 아이디입니다.", StatusEnum.CONFLICT);
		}
		if (!isValidatePassword(signUpDTO.getPassword())) {
			throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
		}
		if (signUpDTO.getNickname().length() >= 12) {
			throw new BusinessException("닉네임은 12자리 이하이어야 합니다.", StatusEnum.BAD_REQUEST);
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
	public MemberReadResponseDto read(Long id) {
		Optional<Member> member = memberRepository.findById(id);
		if (member.isEmpty()) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		return MemberReadResponseDto.builder()
				.id(member.get().getId())
				.nickname(member.get().getNickname())
				.build();
	}

	@Override
	public int count() {
		return (int)memberRepository.count();
	}

	@Override
	public List<Member> list() {
		return memberRepository.findAll();
	}

	@Override
	public MemberUpdateResponseDto update(String username, Long userId, MemberUpdateRequestDto memberUpdateRequestDto) {
		Member member = memberRepository.findByUsername(username);
		if (member == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		if (!member.getId().equals(userId)) {
			throw new BusinessException("수정 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		if (memberUpdateRequestDto.getNickname() != null) {
			if (isDuplicateNickname(memberUpdateRequestDto.getNickname())) {
				throw new BusinessException("이미 존재하는 닉네임입니다.", StatusEnum.CONFLICT);
			}
			member.setNickname(memberUpdateRequestDto.getNickname());
		}
		if (memberUpdateRequestDto.getPassword() != null) {
			if (!isValidatePassword(memberUpdateRequestDto.getPassword())) {
				throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
			}
			member.setPassword(passwordEncoder.encode(memberUpdateRequestDto.getPassword()));
		}
		memberRepository.save(member);
		return MemberUpdateResponseDto.builder()
				.id(member.getId())
				.nickname(member.getNickname())
				.build();
	}

	public boolean isDuplicateNickname(String nickname) {
		return memberRepository.findByNickname(nickname) != null;
	}

	@Override
	public MemberDeleteResponseDto delete(Long id) {
		Member member = memberRepository.findById(id)
				.orElseThrow(() -> new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND));

		if (!member.getDeletedAt().equals(null)) {
			throw new BusinessException("이미 삭제된 회원입니다.", StatusEnum.NOT_FOUND);
		}

		member.setDeletedAt(LocalDateTime.now());
		memberRepository.save(member);

		return MemberDeleteResponseDto.builder()
				.id(id)
				.build();
	}

}