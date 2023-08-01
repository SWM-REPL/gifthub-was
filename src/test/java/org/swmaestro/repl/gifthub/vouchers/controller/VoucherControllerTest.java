package org.swmaestro.repl.gifthub.vouchers.controller;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseResponseDto;
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

	/*
	기프티콘 상세 조회 테스트
	 */
	@Test
	void readVoucherTest() throws Exception {
		// given
		Long voucherId = 1L;
		String username = "user11";
		VoucherReadResponseDto voucherReadResponseDto = VoucherReadResponseDto.builder()
				.id(1L)
				.barcode("012345678910")
				.expiresAt("2023-06-15")
				.build();

		when(voucherService.read(voucherId, username)).thenReturn(voucherReadResponseDto);
		//when
		VoucherReadResponseDto result = voucherService.read(voucherId, username);

		// then
		assertEquals(voucherId, result.getId());
	}

	/*
	기프티콘 목록 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void listVoucherTest() throws Exception {
		String accessToken = "my_awesome_access_token";
		String username = "이진우";

		List<Long> voucherIdList = new ArrayList<>();
		voucherIdList.add(1L);
		voucherIdList.add(2L);

		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(voucherService.list(username)).thenReturn(voucherIdList);

		mockMvc.perform(get("/vouchers")
						.header("Authorization", "Bearer " + accessToken))
				.andExpect(status().isOk());
	}

	/*
	기프티콘 정보 수정 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void voucherUpdateTest() throws Exception {
		// given
		VoucherUpdateRequestDto voucherUpdateRequestDto = VoucherUpdateRequestDto.builder()
				.brandName("스타벅스")
				.productName("아이스 아메리카노 T")
				.barcode("012345678910")
				.expiresAt("2023-06-15")
				.build();

		VoucherSaveResponseDto voucherSaveResponseDto = VoucherSaveResponseDto.builder()
				.id(1L)
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.update(any(), any(VoucherUpdateRequestDto.class))).thenReturn(voucherSaveResponseDto);

		// then
		mockMvc.perform(patch("/vouchers/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucherSaveResponseDto)))
				.andExpect(status().isOk());
	}

	/*
	기프티콘 사용 등록 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void useVoucherTest() throws Exception {
		// given
		Long voucherId = 1L;
		VoucherUseRequestDto voucherUseRequestDto = VoucherUseRequestDto.builder()
				.id(1L)
				.amount(5000)
				.place("스타벅스 아남타워점")
				.build();

		VoucherUseResponseDto voucherUseResponseDto = VoucherUseResponseDto.builder()
				.usageId(1L)
				.voucherId(1L)
				.balance(20000)
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.use(anyString(), eq(voucherId), any(VoucherUseRequestDto.class)))
				.thenReturn(voucherUseResponseDto);

		// then
		mockMvc.perform(post("/vouchers/1/usage")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucherUseRequestDto)))
				.andExpect(status().isOk());
	}
}
