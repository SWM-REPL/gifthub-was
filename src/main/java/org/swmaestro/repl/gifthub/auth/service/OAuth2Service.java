package org.swmaestro.repl.gifthub.auth.service;

import java.net.MalformedURLException;

import org.swmaestro.repl.gifthub.auth.dto.OAuth2UserInfoDto;
import org.swmaestro.repl.gifthub.auth.dto.TokenDto;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.entity.OAuth;

public interface OAuth2Service {
	OAuth2UserInfoDto getUserInfo(TokenDto tokenDto) throws MalformedURLException;

	OAuth save(Member member, OAuth2UserInfoDto oAuth2UserInfoDto);

	boolean isExists(Member member);

	boolean isExists(OAuth2UserInfoDto oAuth2UserInfoDto);
}
