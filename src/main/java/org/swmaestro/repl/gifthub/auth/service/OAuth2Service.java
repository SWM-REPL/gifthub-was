package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.OAuth2UserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;

public interface OAuth2Service {
	OAuth2UserInfoDto getUserInfo(String token);

	OAuth save(Member member, String platformId);

	boolean isExists(Member member);
}
