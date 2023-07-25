package org.swmaestro.repl.gifthub.auth.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.auth.entity.UserToken;

public interface UserTokenRepository extends JpaRepository<UserToken, Long> {
}
