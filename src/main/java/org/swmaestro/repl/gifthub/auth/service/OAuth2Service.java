package org.swmaestro.repl.gifthub.auth.service;

import org.swmaestro.repl.gifthub.auth.dto.OAuthTokenDto;
import org.swmaestro.repl.gifthub.auth.dto.OAuthUserInfoDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;

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
	 * @param member
	 * @param oAuthUserInfoDto
	 * @return
	 */

	OAuth create(Member member, OAuthUserInfoDto oAuthUserInfoDto);

	/**
	 * OAuth 플랫폼 정보를 가져온다.
	 * @param oAuthUserInfoDto
	 * @return
	 */
	OAuth read(OAuthUserInfoDto oAuthUserInfoDto);

	/**
	 * 연동된 OAuth 계정 정보를 삭제한다.
	 * @param member
	 * @return
	 */
	OAuth delete(Member member);

	/**
	 * OAuth 플랫폼 존재 여부를 확인한다.
	 * @param member
	 * @return
	 */
	boolean isExists(Member member);

	/**
	 * OAuth 플랫폼 존재 여부를 확인한다.
	 * @param oAuthUserInfoDto
	 * @return
	 */
	boolean isExists(OAuthUserInfoDto oAuthUserInfoDto);
}
