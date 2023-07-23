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
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
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

	@MockBean
	private JwtProvider jwtProvider;

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void saveVoucher() throws Exception {
		// given
		VoucherSaveRequestDto voucher = VoucherSaveRequestDto.builder()
			.brandName("스타벅스")
			.productName("아이스 아메리카노 T")
			.barcode("012345678910")
			.expiresAt("2023-06-15")
			.imageUrl("https://s3.ap-northeast-2.amazonaws.com/gifthub-voucher/1623777600000_스타벅스_아이스아메리카노T.png")
			.build();

		VoucherSaveResponseDto voucherSaveResponseDto = VoucherSaveResponseDto.builder()
			.id(1L)
			.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.save(anyString(), any(VoucherSaveRequestDto.class))).thenReturn(voucherSaveResponseDto);

		// then
		mockMvc.perform(post("/vouchers")
				.header("Authorization", "Bearer my_awesome_access_token")
				.contentType(MediaType.APPLICATION_JSON)
				.content(objectMapper.writeValueAsString(voucher)))
			.andExpect(status().isOk());
	}
}