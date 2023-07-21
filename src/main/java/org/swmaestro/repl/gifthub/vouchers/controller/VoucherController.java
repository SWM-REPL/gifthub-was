package org.swmaestro.repl.gifthub.vouchers.controller;

import java.io.IOException;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;

@RestController
@RequestMapping("/vouchers")
@RequiredArgsConstructor
@Tag(name = "Vouchers", description = "기프티콘 관련 API")
public class VoucherController {
	private final VoucherService voucherService;

	@PostMapping
	public VoucherSaveResponseDto saveVoucher(@RequestBody VoucherDto voucherDto) throws IOException {
		return voucherService.save(voucherDto);
	}

}
