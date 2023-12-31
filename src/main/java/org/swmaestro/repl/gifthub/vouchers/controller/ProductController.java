package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.SuccessMessage;
import org.swmaestro.repl.gifthub.vouchers.dto.ProductReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.ProductService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/products")
@RequiredArgsConstructor
@Tag(name = "Products", description = "상품 관련 API")
public class ProductController {
	private final ProductService productService;

	@GetMapping("/{productId}")
	@Operation(summary = "상품 상세 조회 메서드", description = "클라이언트에서 요청한 상품 상세 정보를 조회하기 위한 메서드입니다. 응답으로 product-response-dto를 반환합니다.")
	@ApiResponses({
			@ApiResponse(responseCode = "200", description = "회원가입 성공"),
			@ApiResponse(responseCode = "400(404)", description = "존재하지 않는 상품 조회"),
	})
	public ResponseEntity<Message> readProduct(HttpServletRequest request, @PathVariable Long productId) throws IOException {
		ProductReadResponseDto readProduct = productService.readById(productId);
		return ResponseEntity.ok(
				SuccessMessage.builder()
						.path(request.getRequestURI())
						.data(readProduct)
						.build());
	}
}
