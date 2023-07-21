package org.swmaestro.repl.gifthub.vouchers.controller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class VoucherControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private VoucherService voucherService;

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void saveVoucher() throws Exception {
		// given
		VoucherDto voucher = VoucherDto.builder()
			.username("이진우")
			.brandName("스타벅스")
			.productName("아이스 아메리카노 T")
			.barcode("012345678910")
			.expiresAt("2023-06-15T05:34:55.746Z")
			.imageFile(null)
			.build();

		VoucherSaveResponseDto voucherSaveResponseDto = VoucherSaveResponseDto.builder()
			.id(1L)
			.build();

		// when
		when(voucherService.save(voucher)).thenReturn(voucherSaveResponseDto);

		// then
		mockMvc.perform(post("/vouchers")
				.header("Authorization", "my_awesome_access_token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voucher)))
			.andExpect(status().isOk());
	}
}