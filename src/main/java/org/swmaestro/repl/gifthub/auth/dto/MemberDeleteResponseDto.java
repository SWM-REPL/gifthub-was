package org.swmaestro.repl.gifthub.auth.dto;

import java.io.Serializable;

import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@ToString
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MemberDeleteResponseDto implements Serializable {
	private Long id;

	@Builder
	public MemberDeleteResponseDto(Long id) {
		this.id = id;
	}
}
