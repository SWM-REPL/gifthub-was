package org.swmaestro.repl.gifthub.util.entity;

import jakarta.persistence.EntityListeners;
import jakarta.persistence.MappedSuperclass;
import jakarta.validation.constraints.NotNull;
import org.springframework.data.annotation.CreatedDate;
import org.springframework.data.annotation.LastModifiedDate;
import org.springframework.data.jpa.domain.support.AuditingEntityListener;

import java.time.LocalDateTime;

@MappedSuperclass
@EntityListeners(AuditingEntityListener.class)
public abstract class BaseTimeEntity {
	@NotNull
	@CreatedDate
	private LocalDateTime createdAt;

	@NotNull
	@LastModifiedDate
	private LocalDateTime updatedAt;

	private LocalDateTime deletedAt;
}
