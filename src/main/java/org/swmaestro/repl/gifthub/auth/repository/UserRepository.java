package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.User;

public interface UserRepository extends JpaRepository<User, Long> {

	User findByUsername(String username);

	User findAllByNickname(String nickname);

	User findByUsernameAndDeletedAtIsNull(String username);

	User findByIdAndDeletedAtIsNull(Long userId);
}
