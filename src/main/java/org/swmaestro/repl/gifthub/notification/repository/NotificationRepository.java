package org.swmaestro.repl.gifthub.notification.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.notification.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
}
