package org.swmaestro.repl.gifthub.vouchers.controller;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.vouchers.dto.*;
import org.swmaestro.repl.gifthub.vouchers.service.StorageService;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import java.io.IOException;
import java.util.List;

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

	@PostMapping("/image")
	@Operation(summary = "Voucher 이미지 등록 메서드", description = "클라이언트에서 요청한 기프티콘 이미지를 Amazon S3에 저장하기 위한 메서드입니다.")
	public S3FileDto saveVoucherImage(@RequestPart("image_file") MultipartFile imageFile) throws IOException {
		return storageService.save(voucherDirName, imageFile);
	}

	@PostMapping
	@Operation(summary = "Voucher 등록 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 저장하기 위한 메서드입니다.")
	public VoucherSaveResponseDto saveVoucher(HttpServletRequest request,
	                                          @RequestBody VoucherSaveRequestDto voucherSaveRequestDto) throws
			IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return voucherService.save(username, voucherSaveRequestDto);
	}

	@GetMapping("/{voucherId}")
	@Operation(summary = "Voucher 상세 조회 메서드", description = "클라이언트에서 요청한 기프티콘 상세 정보를 조회하기 위한 메서드입니다.")
	public VoucherReadResponseDto readVoucher(@PathVariable Long voucherId) throws IOException {
		return voucherService.read(voucherId);
	}

	@GetMapping
	@Operation(summary = "Voucher 목록 조회 메서드", description = "클라이언트에서 요청한 사용자 별 기프티콘 목록 정보를 조회하기 위한 메서드입니다.")
	public List<VoucherReadResponseDto> listVoucher(HttpServletRequest request) {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return voucherService.list(username);
	}

	@PatchMapping("/{voucherId}")
	@Operation(summary = "Voucher 수정 메서드", description = "클라이언트에서 요청한 기프티콘 정보를 수정하기 위한 메서드입니다.")
	public VoucherSaveResponseDto updateVoucher(@PathVariable Long voucherId,
	                                            @RequestBody VoucherUpdateRequestDto voucherUpdateRequestDto) throws IOException {
		return voucherService.update(voucherId, voucherUpdateRequestDto);
	}

	@PostMapping("/{voucherId}/usage")
	@Operation(summary = "Voucher 사용 메서드", description = "클라이언트에서 요청한 기프티콘 사용 정보를 저장하기 위한 메서드입니다.")
	public VoucherUseResponseDto useVoucher(HttpServletRequest request, @PathVariable Long voucherId,
	                                        @RequestBody VoucherUseRequestDto voucherUseRequestDto) throws IOException {
		String username = jwtProvider.getUsername(jwtProvider.resolveToken(request).substring(7));
		return voucherService.use(username, voucherId, voucherUseRequestDto);
	}
}