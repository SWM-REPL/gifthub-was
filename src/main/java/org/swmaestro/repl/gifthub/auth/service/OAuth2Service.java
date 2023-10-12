package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;
import org.swmaestro.repl.gifthub.auth.entity.User;

/**
 * OAuth2 인증을 위한 인터페이스
 */
public interface OAuth2Service {
	/**
	 * 토큰을 이용하여 사용자 정보를 가져온다.
	 * @param oAuthTokenDto
	 * @return
	 */
	OAuthUserInfoDto getUserInfo(OAuthTokenDto oAuthTokenDto);

	/**
	 * OAuth 플랫폼 정보를 저장한다.
	 * @param user
	 * @param oAuthUserInfoDto
	 * @return
	 */

	OAuth create(User user, OAuthUserInfoDto oAuthUserInfoDto);

	/**
	 * OAuth 플랫폼 정보를 가져온다.
	 * @param oAuthUserInfoDto
	 * @return
	 */
	OAuth read(OAuthUserInfoDto oAuthUserInfoDto);

	/**
	 * 연동된 OAuth 계정 정보를 삭제한다.
	 * @param user
	 * @return
	 */
	OAuth delete(User user);

	/**
	 * OAuth 플랫폼 존재 여부를 확인한다.
	 * @param user
	 * @return
	 */
	boolean isExists(User user);

	/**
	 * OAuth 플랫폼 존재 여부를 확인한다.
	 * @param oAuthUserInfoDto
	 * @return
	 */
	boolean isExists(OAuthUserInfoDto oAuthUserInfoDto);
}
