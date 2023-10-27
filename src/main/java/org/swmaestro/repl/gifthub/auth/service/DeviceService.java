package org.swmaestro.repl.gifthub.auth.service;

import java.util.List;
import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.JwtTokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Device;
import org.swmaestro.repl.gifthub.auth.repository.DeviceRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import jakarta.transaction.Transactional;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;

@Service
@RequiredArgsConstructor
@Slf4j
public class DeviceService {
	private final JwtProvider jwtProvider;
	private final DeviceRepository deviceRepository;

	@Transactional
	public void create(JwtTokenDto jwtTokenDto, String deviceToken, String fcmToken) {
		Long userId = jwtProvider.getUserId(jwtTokenDto.getRefreshToken());
		Device device = Device.builder()
				.refreshToken(jwtTokenDto.getRefreshToken())
				.userId(userId)
				.deviceToken(deviceToken)
				.fcmToken(fcmToken)
				.build();

		if (deviceRepository.findByUserIdAndDeviceToken(userId, deviceToken).isPresent()) {
			deviceRepository.deleteByUserIdAndDeviceToken(userId, deviceToken);
		}
		deviceRepository.save(device);
	}

	public String createNewAccessTokenByValidateRefreshToken(String refreshToken) {
		if (jwtProvider.validateToken(refreshToken)) {
			refreshToken = refreshToken.substring(7);
			return jwtProvider.reissueAccessToken(refreshToken);
		}
		return null;
	}

	public String createNewRefreshTokenByValidateRefreshToken(String refreshToken) {
		if (jwtProvider.validateToken(refreshToken)) {
			refreshToken = refreshToken.substring(7);
			return jwtProvider.generateRefreshToken(jwtProvider.getUsername(refreshToken), jwtProvider.getUserId(refreshToken));
		}
		return null;
	}

	/**
	 * Device 삭제
	 * @param userId
	 * @param deviceToken
	 */
	public void delete(Long userId, String deviceToken) {
		Optional<Device> refreshToken = deviceRepository.findByUserIdAndDeviceToken(userId, deviceToken);
		if (refreshToken.isPresent()) {
			deviceRepository.delete(refreshToken.get());
		} else {
			throw new BusinessException("존재하지 않는 사용자 입니다.", StatusEnum.UNAUTHORIZED);
		}
	}

	public List<Device> list(Long userId) {
		return deviceRepository.findAllByUserId(userId);
	}

	public List<Device> list() {
		return deviceRepository.findAll();
	}

	/**
	 * Device 삭제
	 */
	public void delete(Long id) {
		deviceRepository.deleteById(id);
	}
}
