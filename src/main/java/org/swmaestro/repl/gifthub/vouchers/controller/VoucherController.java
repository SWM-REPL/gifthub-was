package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.http.HttpStatus;
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
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.PresignedUrlResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.service.StorageService;
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

	@GetMapping("/images")
	@Operation(summary = "Voucher 이미지 등록 메서드", description = "클라이언트에서 요청한 기프티콘 이미지를 Amazon S3에 저장하기 위한 메서드입니다. 요청 시 S3 PreSigned URL이 반환됩니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "성공적으로 S3 Presigned URL 반환"),
	})
	public ResponseEntity<Message> saveVoucherImage() throws IOException {
		PresignedUrlResponseDto presignedUrlResponseDto = PresignedUrlResponseDto.builder()
				.presignedUrl(storageService.getPresignedUrlForSaveVoucher("voucher", "PNG"))
				.build();

		return ResponseEntity.ok(Message.builder()
				.status(StatusEnum.OK)
				.message("성공적으로 S3 Presigned URL 반환되었습니다!")
				.data(presignedUrlResponseDto)
				.build()
		);
	}

	@PostMapping
	@Operation(summary = "Voucher 등록 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 저장하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 등록 성공"),
			@ApiResponse(responseCode = "400(404-1)", description = "존재하지 않는 브랜드 입력"),
			@ApiResponse(responseCode = "400(404-2)", description = "존재하지 않는 상품 입력"),
	})
	public ResponseEntity<Message> saveVoucher(HttpServletRequest request,
			@RequestBody VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘이 성공적으로 등록되었습니다!")
						.data(voucherService.save(username, voucherSaveRequestDto))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
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
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘이 성공적으로 조회되었습니다!")
						.data(voucherService.read(voucherId, username))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@GetMapping
	@Operation(summary = "Voucher 목록 조회 메서드", description = "클라이언트에서 요청한 사용자 별 기프티콘 목록 정보를 조회하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 목록 조회 성공"),
			@ApiResponse(responseCode = "400(403)", description = "자신의 것이 아닌 기프티콘 조회 시도"),
	})
	public ResponseEntity<Message> listVoucher(HttpServletRequest request, @RequestParam(value = "user_id", required = true) Long memberId) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘 목록이 성공적으로 조회되었습니다!")
						.data(voucherService.list(memberId, username))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}

	@PatchMapping("/{voucherId}")
	@Operation(summary = "Voucher 수정 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 수정하기 위한 메서드입니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "기프티콘 수정 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 기프티콘 조회 시도"),
	})
	public ResponseEntity<Message> updateVoucher(@PathVariable Long voucherId,
			@RequestBody VoucherUpdateRequestDto voucherUpdateRequestDto) throws IOException {
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘 목록이 성공적으로 수정되었습니다!")
						.data(voucherService.update(voucherId, voucherUpdateRequestDto))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
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
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘이 성공적으로 사용되었습니다!")
						.data(voucherService.use(username, voucherId, voucherUseRequestDto))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
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
		return ResponseEntity
				.ok(Message.builder()
						.status(StatusEnum.OK)
						.message("기프티콘이 성공적으로 삭제되었습니다!")
						// .data()
						.build());
	}
}