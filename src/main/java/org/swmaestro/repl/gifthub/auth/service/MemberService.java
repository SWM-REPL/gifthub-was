package org.swmaestro.repl.gifthub.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class MemberService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final OAuthService oAuthService;

	public Member passwordEncryption(Member member) {
		return Member.builder()
				.username(member.getUsername())
				.password(passwordEncoder.encode(member.getPassword()))
				.nickname(member.getNickname())
				.build();
	}

	public Member create(Member member) {
		if (isDuplicateUsername(member.getUsername())) {
			throw new BusinessException("이미 존재하는 아이디입니다.", StatusEnum.CONFLICT);
		}
		if (!isValidatePassword(member.getPassword())) {
			throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
		}
		if (member.getNickname().length() >= 12) {
			throw new BusinessException("닉네임은 12자리 이하이어야 합니다.", StatusEnum.BAD_REQUEST);
		}

		Member encodedMember = passwordEncryption(member);
		return memberRepository.save(encodedMember);
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

	public Member read(String username) {
		Member member = memberRepository.findByUsername(username);
		if (member == null) {
			return null;
		}
		return member;
	}

	public MemberReadResponseDto read(Long id) {
		Optional<Member> member = memberRepository.findById(id);
		if (member.isEmpty()) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		return MemberReadResponseDto.builder()
				.id(member.get().getId())
				.nickname(member.get().getNickname())
				.username(member.get().getUsername())
				.build();
	}

	public int count() {
		return (int)memberRepository.count();
	}

	public List<Member> list() {
		return memberRepository.findAll();
	}

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

	public String generateOAuthUsername() {
		return UUID.randomUUID().toString();
	}

	public OAuth createOAuthInfo(Member member, OAuthPlatform oAuthPlatform, OAuthTokenDto oAuthTokenDto) {
		OAuthUserInfoDto oAuthUserInfoDto = oAuthService.getUserInfo(oAuthTokenDto, oAuthPlatform);
		return oAuthService.create(member, oAuthUserInfoDto, oAuthPlatform);
	}
}