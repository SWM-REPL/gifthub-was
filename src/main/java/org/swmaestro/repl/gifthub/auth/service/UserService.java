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
import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.UserDeleteResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserInfoResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserReadResponseDto;
import org.swmaestro.repl.gifthub.auth.dto.UserUpdateRequestDto;
import org.swmaestro.repl.gifthub.auth.dto.UserUpdateResponseDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.DeviceTokenRepository;
import org.swmaestro.repl.gifthub.auth.repository.UserRepository;
import org.swmaestro.repl.gifthub.auth.type.OAuthPlatform;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class UserService implements UserDetailsService {
	private final UserRepository userRepository;
	private final PasswordEncoder passwordEncoder;
	private final OAuthService oAuthService;
	private final DeviceTokenRepository deviceTokenRepository;
	private final Pattern UUID_REGEX = Pattern.compile("^[0-9a-fA-F]{8}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{4}-[0-9a-fA-F]{12}$");

	public User passwordEncryption(User user) {
		return User.builder()
				.username(user.getUsername())
				.password(passwordEncoder.encode(user.getPassword()))
				.nickname(user.getNickname())
				.role(user.getRole())
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
		return userRepository.save(encodedUser);
	}

	public boolean isDuplicateUsername(String username) {
		return userRepository.findByUsernameAndDeletedAtIsNull(username) != null;
	}

	public boolean isValidatePassword(String password) {
		String regex = "^(?=.*[a-zA-Z])(?=.*\\d)(?=.*[@#$%^&+=_\\-!]).{8,64}$";
		Pattern pattern = Pattern.compile(regex);
		Matcher matcher = pattern.matcher(password);

		return matcher.matches();
	}

	public User read(String username) {
		User user = userRepository.findByUsernameAndDeletedAtIsNull(username);
		if (user == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		return user;
	}

	public UserReadResponseDto read(Long id) {
		Optional<User> user = userRepository.findById(id);
		if (user.isEmpty() || !user.get().isEnabled()) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		return UserReadResponseDto.builder()
				.id(user.get().getId())
				.nickname(user.get().getNickname())
				.username(user.get().getUsername())
				.build();
	}

	public int count() {
		return (int)userRepository.count();
	}

	public List<User> list() {
		return userRepository.findAll().stream()
				.filter(user -> user.getDeletedAt() == null)
				.toList();
	}

	public UserUpdateResponseDto update(String username, Long userId, UserUpdateRequestDto userUpdateRequestDto) {
		User user = userRepository.findByUsernameAndDeletedAtIsNull(username);
		if (user == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}
		if (!user.getId().equals(userId)) {
			throw new BusinessException("수정 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		if (userUpdateRequestDto.getNickname().length() < 2 || userUpdateRequestDto.getNickname().length() > 12) {
			throw new BusinessException("닉네임은 2자 이상 12자 이하로 입력해주세요.", StatusEnum.BAD_REQUEST);
		}
		if (userUpdateRequestDto.getNickname() != null) {
			if (isDuplicateNickname(userUpdateRequestDto.getNickname())) {
				throw new BusinessException("이미 존재하는 닉네임입니다.", StatusEnum.CONFLICT);
			}
			user.setNickname(userUpdateRequestDto.getNickname());
		}
		if (userUpdateRequestDto.getPassword() != null) {
			if (!isValidatePassword(userUpdateRequestDto.getPassword())) {
				throw new BusinessException("비밀번호는 영문, 숫자, 특수문자를 포함한 8자리 이상이어야 합니다.", StatusEnum.BAD_REQUEST);
			}
			user.setPassword(passwordEncoder.encode(userUpdateRequestDto.getPassword()));
		}
		userRepository.save(user);
		return UserUpdateResponseDto.builder()
				.id(user.getId())
				.nickname(user.getNickname())
				.build();
	}

	public boolean isDuplicateNickname(String nickname) {
		return userRepository.findByNickname(nickname) != null;
	}

	public UserDeleteResponseDto delete(Long id) {
		User user = userRepository.findById(id)
				.orElseThrow(() -> new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND));

		if (user.getDeletedAt() != null) {
			throw new BusinessException("이미 삭제된 회원입니다.", StatusEnum.NOT_FOUND);
		}

		user.setDeletedAt(LocalDateTime.now());
		userRepository.save(user);

		deleteOAuthInfo(user);
		return UserDeleteResponseDto.builder()
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
		if (UUID_REGEX.matcher(user.getUsername()).matches() && oAuthService.count(user) <= 1) {
			throw new BusinessException("최소 하나 이상의 OAuth 연동 계정이 존재해야 합니다.", StatusEnum.BAD_REQUEST);
		}
		return oAuthService.delete(user, oAuthPlatform);
	}

	@Override
	public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
		User user = userRepository.findByUsernameAndDeletedAtIsNull(username);
		if (user == null) {
			throw new UsernameNotFoundException("존재하지 않는 회원입니다.");
		}
		return user;
	}

	public List<OAuth> deleteOAuthInfo(User user) {
		return oAuthService.delete(user);
	}

	public UserInfoResponseDto readInfo(String username) {
		User user = read(username);
		UserInfoResponseDto userInfoResponseDto = UserInfoResponseDto.builder()
				.id(user.getId())
				.username(username)
				.nickname(user.getNickname())
				.oauth(oAuthService.list(user))
				.allowNotifications(isExistDeviceToken(user))
				.anonymous(user.isAnonymous())
				.build();
		return userInfoResponseDto;
	}

	/**
	 * DeviceToken 조회 메서드 (user)
	 */
	public boolean isExistDeviceToken(User user) {
		return !deviceTokenRepository.findAllByUser(user).isEmpty();
	}
}