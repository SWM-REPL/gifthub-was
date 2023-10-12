package org.swmaestro.repl.gifthub.auth.service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;
import java.util.UUID;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.MemberDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.MemberUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.MemberRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final MemberRepository memberRepository;
	private final PasswordEncoder passwordEncoder;
	private final OAuthService oAuthService;

	public User passwordEncryption(User user) {
		return User.builder()
				.username(user.getUsername())
				.password(passwordEncoder.encode(user.getPassword()))
				.nickname(user.getNickname())
				.build();
	}

	public User create(User user) {
		if (isDuplicateUsername(user.getUsername())) {
			throw new BusinessException("이미 존재하는 아이디입니다.", StatusEnum.CONFLICT);
		}
		if (!isValidatePassword(user.getPassword())) {
			throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
		}
		if (user.getNickname().length() >= 12) {
			throw new BusinessException("닉네임은 12자리 이하이어야 합니다.", StatusEnum.BAD_REQUEST);
		}

		User encodedUser = passwordEncryption(user);
		return memberRepository.save(encodedUser);
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

	public User read(String username) {
		User user = memberRepository.findByUsername(username);
		if (user == null) {
			return null;
		}
		return user;
	}

	public MemberReadResponseDto read(Long id) {
		Optional<User> member = memberRepository.findById(id);
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

	public List<User> list() {
		return memberRepository.findAll();
	}

	public MemberUpdateResponseDto update(String username, Long userId, MemberUpdateRequestDto memberUpdateRequestDto) {
		User user = memberRepository.findByUsername(username);
		if (user == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		if (!user.getId().equals(userId)) {
			throw new BusinessException("수정 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		if (memberUpdateRequestDto.getNickname() != null) {
			if (isDuplicateNickname(memberUpdateRequestDto.getNickname())) {
				throw new BusinessException("이미 존재하는 닉네임입니다.", StatusEnum.CONFLICT);
			}
			user.setNickname(memberUpdateRequestDto.getNickname());
		}
		if (memberUpdateRequestDto.getPassword() != null) {
			if (!isValidatePassword(memberUpdateRequestDto.getPassword())) {
				throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
			}
			user.setPassword(passwordEncoder.encode(memberUpdateRequestDto.getPassword()));
		}
		memberRepository.save(user);
		return MemberUpdateResponseDto.builder()
				.id(user.getId())
				.nickname(user.getNickname())
				.build();
	}

	public boolean isDuplicateNickname(String nickname) {
		return memberRepository.findByNickname(nickname) != null;
	}

	public MemberDeleteResponseDto delete(Long id) {
		User user = memberRepository.findById(id)
				.orElseThrow(() -> new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND));

		if (!user.getDeletedAt().equals(null)) {
			throw new BusinessException("이미 삭제된 회원입니다.", StatusEnum.NOT_FOUND);
		}

		user.setDeletedAt(LocalDateTime.now());
		memberRepository.save(user);

		return MemberDeleteResponseDto.builder()
				.id(id)
				.build();
	}

	public String generateOAuthUsername() {
		return UUID.randomUUID().toString();
	}

	public OAuth createOAuthInfo(User user, OAuthPlatform oAuthPlatform, OAuthTokenDto oAuthTokenDto) {
		OAuthUserInfoDto oAuthUserInfoDto = oAuthService.getUserInfo(oAuthTokenDto, oAuthPlatform);
		return oAuthService.create(user, oAuthUserInfoDto, oAuthPlatform);
	}

	public OAuth deleteOAuthInfo(User user, OAuthPlatform oAuthPlatform) {
		return oAuthService.delete(user, oAuthPlatform);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		return null;
	}
}