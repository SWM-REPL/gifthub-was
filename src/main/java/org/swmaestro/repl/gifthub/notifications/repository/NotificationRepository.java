package org.swmaestro.repl.gifthub.notifications.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.notifications.entity.Notification;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
	List<Notification> findAllByReceiverUsername(String username);
}
