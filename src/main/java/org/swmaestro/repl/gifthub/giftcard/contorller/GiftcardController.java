package org.swmaestro.repl.gifthub.giftcard.contorller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.giftcard.dto.GiftcardResponseDto;
import org.swmaestro.repl.gifthub.giftcard.service.GiftcardService;
import org.swmaestro.repl.gifthub.util.BasicAuthenticationDecoder;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/giftcards")
@RequiredArgsConstructor
@Tag(name = "GiftCard", description = "공유하기 관련 API")
public class GiftcardController {
	private final GiftcardService giftcardService;

	@GetMapping("/{id}")
	@Operation(summary = "공유 정보 요청 메서드", description = "클라이언트에서 요청한 공유 정보를 전달하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "공유하기 정보 조회 성공"),
			@ApiResponse(responseCode = "400", description = "만료된 공유하기 정보 접근"),
			@ApiResponse(responseCode = "403", description = "일치하지 않는 비밀번호 입력"),
			@ApiResponse(responseCode = "404", description = "존재하지 않는 공유하기 정보 접근")
	})
	public ResponseEntity<Message> read(HttpServletRequest request, @PathVariable String id) {
		GiftcardResponseDto giftcardResponseDto = giftcardService.read(id, BasicAuthenticationDecoder.decode(request));
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(giftcardResponseDto)
						.build());
	}
}
