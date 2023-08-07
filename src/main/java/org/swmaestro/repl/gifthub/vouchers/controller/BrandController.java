package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.util.HttpJsonHeaders;
import org.swmaestro.repl.gifthub.util.Message;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.service.BrandService;

import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/brand")
@RequiredArgsConstructor
@Tag(name = "Brand", description = "브랜드 관련 API")
public class BrandController {
	private final BrandService brandService;

	@GetMapping("/{brandId}")
	@Operation(summary = "브랜드 상세 조회 메서드", description = "클라이언트에서 요청한 브랜드 상세 정보를 조회하기 위한 메서드입니다. 응답으로 brand-entity를 반환합니다.")
	public ResponseEntity<Message> readBrand(@PathVariable Long brandId) throws IOException {
		return new ResponseEntity<>(
				Message.builder()
						.status(StatusEnum.OK)
						.message("성공적으로 조회되었습니다!")
						.data(brandService.readById(brandId))
						.build(),
				new HttpJsonHeaders(),
				HttpStatus.OK
		);
	}
}

