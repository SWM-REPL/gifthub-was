package org.swmaestro.repl.gifthub.auth.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.Device;

public interface DeviceRepository extends JpaRepository<Device, Long> {

	Optional<Device> findByUserIdAndDeviceToken(Long userId, String deviceToken);

	void deleteByUserIdAndDeviceToken(Long userId, String deviceToken);

	Device findByRefreshToken(String refreshToken);

	List<Device> findAllByUserId(Long userId);
}
