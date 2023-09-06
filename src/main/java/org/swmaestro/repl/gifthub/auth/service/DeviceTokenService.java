package org.swmaestro.repl.gifthub.auth.service;

import java.util.List;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.auth.dto.MemberReadResponseDto;
import org.swmaestro.repl.gifthub.auth.entity.DeviceToken;
import org.swmaestro.repl.gifthub.auth.entity.Member;
import org.swmaestro.repl.gifthub.auth.repository.DeviceTokenRepository;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class DeviceTokenService {
	private final DeviceTokenRepository deviceTokenRepository;
	private final MemberService memberService;

	/*
	 * DeviceToken 저장 메서드
	 */
	public DeviceToken save(String username, String token) {
		Member member = memberService.read(username);
		DeviceToken deviceToken = DeviceToken.builder()
				.member(member)
				.token(token)
				.build();
		return deviceTokenRepository.save(deviceToken);
	}

	/*
	 * DeviceToken 존재 여부 반환 메서드 (토큰만으로)
	 */
	public boolean isExist(String deviceToken) {
		return deviceTokenRepository.findByToken(deviceToken).isPresent();
	}

	/*
	 * DeviceToken 존재 여부 반환 메서드 (회원 아이디와 토큰으로)
	 */
	public boolean isExist(Long memberId, String deviceToken) {
		MemberReadResponseDto memberDto = memberService.read(memberId);
		Member member = memberService.read(memberDto.getUsername());

		return deviceTokenRepository.findByMemberAndToken(member, deviceToken).isPresent();
	}

	/*
	 * DeviceToken 전체 조회 메서드
	 */
	public List<DeviceToken> list(Long memberId) {
		MemberReadResponseDto memberDto = memberService.read(memberId);
		Member member = memberService.read(memberDto.getUsername());
		return deviceTokenRepository.findAllByMember(member);
	}

	/*
	 * DeviceToken 선택 조회 메서드 (token)
	 */
	public DeviceToken read(String token) {
		return deviceTokenRepository.findByToken(token)
				.orElseThrow(() -> new BusinessException("존재하지 않는 토큰입니다.", StatusEnum.INTERNAL_SERVER_ERROR));
	}

	/*
	 * DeviceToken 선택 조회 메서드 (deviceTokenId)
	 */
	public DeviceToken read(Long deviceTokenId) {
		return deviceTokenRepository.findById(deviceTokenId)
				.orElseThrow(() -> new BusinessException("존재하지 않는 토큰입니다.", StatusEnum.INTERNAL_SERVER_ERROR));
	}

	/*
	 * DeviceToken 삭제 메서드 (token)
	 */
	public void delete(String token) {
		DeviceToken deviceToken = read(token);
		deviceTokenRepository.delete(deviceToken);
	}
}
