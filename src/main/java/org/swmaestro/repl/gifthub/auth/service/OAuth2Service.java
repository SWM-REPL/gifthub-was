package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;

public interface OAuth2Service {
	OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto);

	OAuth create(Member member, OAuthUserInfoDto oAuthUserInfoDto);

	OAuth read(OAuthUserInfoDto oAuthUserInfoDto);

	boolean isExists(Member member);

	boolean isExists(OAuthUserInfoDto oAuthUserInfoDto);
}
