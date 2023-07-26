package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;

public interface DeviceTokenRepository extends JpaRepository<DeviceToken, Long> {
}
