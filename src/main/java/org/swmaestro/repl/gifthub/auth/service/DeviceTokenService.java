package org.swmaestro.repl.gifthub.auth.service;

import java.time.LocalDateTime;
import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.UserReadResponseDto;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.entity.User;
import org.swmaestro.repl.gifthub.auth.repository.DeviceTokenRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
	private final DeviceTokenRepository deviceTokenRepository;
	private final UserService userService;

	/*
	 * DeviceToken 저장 메서드
	 */
	public void save(String username, String token) {
		User user = userService.read(username);

		if (user == null) {
			throw new BusinessException("존재하지 않는 회원입니다.", StatusEnum.NOT_FOUND);
		}

		if (token == null) {
			throw new BusinessException("토큰이 존재하지 않습니다.", StatusEnum.BAD_REQUEST);
		}

		DeviceToken deviceToken;
		if (!isExist(token)) {
			deviceToken = DeviceToken.builder()
					.user(user)
					.token(token)
					.build();

		} else {
			deviceToken = read(token);
			deviceToken.setUpdatedAt(LocalDateTime.now());
			deviceToken.setUser(user);
		}
		deviceTokenRepository.save(deviceToken);
	}

	/*
	 * DeviceToken 존재 여부 반환 메서드 (토큰만으로)
	 */
	public boolean isExist(String deviceToken) {
		return deviceTokenRepository.findByToken(deviceToken).isPresent();
	}

	/*
	 * DeviceToken 존재 여부 반환 메서드 (회원 아이디와 토큰으로)
	 */
	public boolean isExist(Long memberId, String deviceToken) {
		UserReadResponseDto memberDto = userService.read(memberId);
		User user = userService.read(memberDto.getUsername());

		return deviceTokenRepository.findByUserAndToken(user, deviceToken).isPresent();
	}

	/**
	 DeviceToken 전체 조회 메서드
	 */
	public List<DeviceToken> list() {
		return deviceTokenRepository.findAll();
	}

	/*
	 * DeviceToken 전체 조회 메서드(memberId)
	 */
	public List<DeviceToken> list(Long memberId) {
		UserReadResponseDto memberDto = userService.read(memberId);
		User user = userService.read(memberDto.getUsername());
		return deviceTokenRepository.findAllByUser(user);
	}

	/*
	 * DeviceToken 선택 조회 메서드 (token)
	 */
	public DeviceToken read(String token) {
		return deviceTokenRepository.findByToken(token)
				.orElseThrow(() -> new BusinessException("존재하지 않는 토큰입니다.", StatusEnum.INTERNAL_SERVER_ERROR));
	}

	/*
	 * DeviceToken 선택 조회 메서드 (deviceTokenId)
	 */
	public DeviceToken read(Long deviceTokenId) {
		return deviceTokenRepository.findById(deviceTokenId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 토큰입니다.", StatusEnum.INTERNAL_SERVER_ERROR));
	}

	/*
	 * DeviceToken 삭제 메서드
	 */
	public void delete(User user, String token) {
		DeviceToken deviceToken = read(token);
		if (!deviceToken.getUser().equals(user)) {
			throw new BusinessException("토큰을 삭제할 권한이 없습니다.", StatusEnum.FORBIDDEN);
		}
		deviceTokenRepository.delete(deviceToken);
	}

	/*
	 * DeviceToken 삭제 메서드 (token)
	 */
	public void delete(String token) {
		DeviceToken deviceToken = read(token);
		deviceTokenRepository.delete(deviceToken);
	}
}
