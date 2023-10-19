package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PatchMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;
import org.swmaestro.repl.gifthub.vouchers.dto.OCRDto;
import org.swmaestro.repl.gifthub.vouchers.dto.PresignedUrlResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherListResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.StorageService;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherSaveService;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "기프티콘 관련 API")
public class VoucherController {
	@Value("${cloud.aws.s3.voucher-dir-name}")
	private String voucherDirName;
	private final VoucherService voucherService;
	private final StorageService storageService;
	private final JwtProvider jwtProvider;
	private final VoucherSaveService voucherSaveService;

	@GetMapping("/images")
	@Operation(summary = "Voucher 이미지 등록 메서드", description = "클라이언트에서 요청한 기프티콘 이미지를 Amazon S3에 저장하기 위한 메서드입니다. 요청 시 S3 PreSigned URL이 반환됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공적으로 S3 Presigned URL 반환"),
	})
	public ResponseEntity<Message> saveVoucherImage(HttpServletRequest request) throws IOException {
		PresignedUrlResponseDto presignedUrlResponseDto = PresignedUrlResponseDto.builder()
				.presignedUrl(storageService.getPresignedUrlForSaveVoucher("voucher", "PNG"))
				.build();
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(presignedUrlResponseDto)
						.build());
	}

	@PostMapping("/manual")
	@Operation(summary = "Voucher 등록 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 수동 저장하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 등록 성공"),
			@ApiResponse(responseCode = "400(404-1)", description = "존재하지 않는 브랜드 입력"),
			@ApiResponse(responseCode = "400(404-2)", description = "존재하지 않는 상품 입력"),
	})
	public ResponseEntity<Message> saveVoucher(HttpServletRequest request,
			@RequestBody VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		VoucherSaveResponseDto voucherSaveResponseDto = voucherService.save(username, voucherSaveRequestDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(voucherSaveResponseDto)
						.build());
	}

	@GetMapping("/{voucherId}")
	@Operation(summary = "Voucher 상세 조회 메서드", description = "클라이언트에서 요청한 기프티콘 상세 정보를 조회하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 상세 조회 성공"),
			@ApiResponse(responseCode = "400(403)", description = "자신의 것이 아닌 기프티콘 조회 시도"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 기프티콘 조회 시도"),
	})
	public ResponseEntity<Message> readVoucher(HttpServletRequest request, @PathVariable Long voucherId) throws
			IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		VoucherReadResponseDto voucherReadResponseDto = voucherService.read(voucherId, username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(voucherReadResponseDto)
						.build());
	}

	@GetMapping
	@Operation(summary = "Voucher 목록 조회 메서드", description = "클라이언트에서 요청한 사용자 별 기프티콘 목록 정보를 조회하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 목록 조회 성공"),
			@ApiResponse(responseCode = "400(403)", description = "자신의 것이 아닌 기프티콘 조회 시도"),
	})
	public ResponseEntity<Message> listVoucher(HttpServletRequest request, @RequestParam(value = "user_id", required = true) Long memberId) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		VoucherListResponseDto voucherListResponseDto = voucherService.list(memberId, username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(voucherListResponseDto)
						.build());
	}

	@PatchMapping("/{voucherId}")
	@Operation(summary = "Voucher 수정 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 수정하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 수정 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 기프티콘 조회 시도"),
	})
	public ResponseEntity<Message> updateVoucher(HttpServletRequest request, @PathVariable Long voucherId,
			@RequestBody VoucherUpdateRequestDto voucherUpdateRequestDto) throws IOException {
		VoucherSaveResponseDto updatedVoucher = voucherService.update(voucherId, voucherUpdateRequestDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(updatedVoucher)
						.build());
	}

	@PostMapping("/{voucherId}/usage")
	@Operation(summary = "Voucher 사용 메서드", description = "클라이언트에서 요청한 기프티콘 사용 정보를 저장하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 사용 히스토리 등록 성공"),
			@ApiResponse(responseCode = "400(400-1)", description = "유효하지 않은 사용 금액 입력"),
			@ApiResponse(responseCode = "400(400-2)", description = "사용처 미입력"),
			@ApiResponse(responseCode = "400(403)", description = "상품권 사용 권한 없음"),
			@ApiResponse(responseCode = "400(404-1)", description = "존재하지 않는 상품권 사용 시도"),
			@ApiResponse(responseCode = "400(404-2)", description = "이미 사용한 상품권 사용 시도"),
	})
	public ResponseEntity<Message> useVoucher(HttpServletRequest request, @PathVariable Long voucherId,
			@RequestBody VoucherUseRequestDto voucherUseRequestDto) throws IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		VoucherUseResponseDto usedVoucher = voucherService.use(username, voucherId, voucherUseRequestDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(usedVoucher)
						.build());
	}

	@DeleteMapping("/{voucherId}")
	@Operation(summary = "Voucher 삭제 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 삭제하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 삭제 성공"),
			@ApiResponse(responseCode = "400(400)", description = "기프티콘 삭제 실패"),
			@ApiResponse(responseCode = "400(403)", description = "자신의 것이 아닌 기프티콘 삭제 시도"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 기프티콘 삭제 시도"),
	})
	public ResponseEntity<Message> deleteVoucher(HttpServletRequest request, @PathVariable Long voucherId) throws
			IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		voucherService.delete(username, voucherId);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@PostMapping
	@Operation(summary = "Voucher 등록 확장 메서드", description = "클라이언트에서 요청한 기프티콘 자동 등록을 위한 메서드입니다. 비동기 방식으로 처리됩니다. 처리가 완료되면 FCM에 처리 완료 알림 요청을 보냅니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200(202)", description = "기프티콘 등록 요청"),
	})
	public ResponseEntity<Message> test(HttpServletRequest request, @RequestBody OCRDto ocrDto) throws IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		voucherSaveService.execute(ocrDto, username);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.build());
	}

	@PostMapping("/{voucherId}/share")
	@Operation(summary = "Voucher 공유 요청 메서드", description = "클라이언트에서 요청한 기프티콘을 공유하기 위한 메서드입니다. 공유 정보를 생성하고 저장합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 공유 요청 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 기프티콘 공유 요청 시도"),
	})
	public ResponseEntity<Message> shareVoucher(HttpServletRequest request, @PathVariable Long voucherId,
			@RequestBody VoucherShareRequestDto voucherShareRequestDto) throws IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		VoucherShareResponseDto voucherShareResponseDto = voucherService.share(username, voucherId, voucherShareRequestDto);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(voucherShareResponseDto)
						.build());
	}
}