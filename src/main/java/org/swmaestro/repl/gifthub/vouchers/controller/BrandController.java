package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;
import org.swmaestro.repl.gifthub.vouchers.dto.BrandReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.BrandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brands")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "브랜드 관련 API")
public class BrandController {
	private final BrandService brandService;

	@GetMapping("/{brandId}")
	@Operation(summary = "브랜드 상세 조회 메서드", description = "클라이언트에서 요청한 브랜드 상세 정보를 조회하기 위한 메서드입니다. 응답으로 brand-response-dto를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "브랜드 조회 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 브랜드 조회 시도")
	})
	public ResponseEntity<Message> readBrand(HttpServletRequest request, @PathVariable Long brandId) throws IOException {
		BrandReadResponseDto readBrand = brandService.readById(brandId);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(readBrand)
						.build());
	}
}

