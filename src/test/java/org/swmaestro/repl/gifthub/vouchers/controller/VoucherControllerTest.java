package org.swmaestro.repl.gifthub.vouchers.controller;

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
import org.swmaestro.repl.gifthub.giftcard.entity.Giftcard;
import org.swmaestro.repl.gifthub.giftcard.service.GiftcardService;
import org.swmaestro.repl.gifthub.util.JwtProvider;
import org.swmaestro.repl.gifthub.vouchers.dto.GptResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.SearchResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherAutoSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherListResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherSaveResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherShareResponseDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUpdateRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseRequestDto;
import org.swmaestro.repl.gifthub.vouchers.dto.VoucherUseResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.GptService;
import org.swmaestro.repl.gifthub.vouchers.service.PendingVoucherService;
import org.swmaestro.repl.gifthub.vouchers.service.SearchService;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherSaveService;
import org.swmaestro.repl.gifthub.vouchers.service.VoucherService;

import com.fasterxml.jackson.databind.ObjectMapper;

import reactor.core.publisher.Mono;

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

	@MockBean
	private VoucherSaveService voucherSaveService;

	@MockBean
	private GptService gptService;

	@MockBean
	private SearchService searchService;
	@MockBean
	private PendingVoucherService pendingVoucherService;

	@MockBean
	private GiftcardService giftcardService;

	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void saveVoucherManual() throws Exception {
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
		mockMvc.perform(post("/vouchers/manual")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucher)))
				.andExpect(status().isOk());
	}

	/*
	기프티콘 상세 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readVoucherTest() throws Exception {
		// given
		Long voucherId = 1L;
		String accessToken = "my_awesome_access_token";
		String username = "user11";
		VoucherReadResponseDto voucherReadResponseDto = VoucherReadResponseDto.builder()
				.id(1L)
				.productId(1L)
				.barcode("012345678910")
				.expiresAt("2023-06-15")
				.build();
		//when
		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(voucherService.read(voucherId, username)).thenReturn(voucherReadResponseDto);

		// then
		mockMvc.perform(get("/vouchers/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucherReadResponseDto)))
				.andExpect(status().isOk());
	}

	/*
	기프티콘 목록 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void listVoucherTest() throws Exception {
		String accessToken = "my_awesome_access_token";
		Long memberId = 1L;
		String username = "이진우";

		List<Long> voucherIdList = new ArrayList<>();
		voucherIdList.add(1L);
		voucherIdList.add(2L);

		int pendingCount = 2;

		VoucherListResponseDto voucherListResponseDto = VoucherListResponseDto.builder()
				.voucherIds(voucherIdList)
				.pendingCount(pendingCount)
				.build();

		when(jwtProvider.resolveToken(any())).thenReturn(accessToken);
		when(jwtProvider.getUsername(anyString())).thenReturn(username);
		when(voucherService.list(memberId, username)).thenReturn(voucherListResponseDto);

		mockMvc.perform(get("/vouchers?user_id=" + memberId)
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
				.amount(5000)
				.place("스타벅스 아남타워점")
				.build();

		VoucherUseResponseDto voucherUseResponseDto = VoucherUseResponseDto.builder()
				.usageId(1L)
				.voucherId(1L)
				.balance(20000)
				.price(25000)
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

	/*
	기프티콘 삭제 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void deleteVoucher() throws Exception {
		// given
		Long voucherId = 1L;
		VoucherUseRequestDto voucherUseRequestDto = VoucherUseRequestDto.builder()
				.amount(5000)
				.place("스타벅스 아남타워점")
				.build();

		// when
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.delete(anyString(), eq(voucherId)))
				.thenReturn(true);

		// then
		mockMvc.perform(delete("/vouchers/" + voucherId)
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}

	/*
	기프티콘 자동 등록 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void saveVoucher() throws Exception {
		//given
		List<String> texts = new ArrayList<>();
		texts.add("스타벅스");
		texts.add("아이스 아메리카노 T");
		texts.add("012345678910");
		texts.add("2023-06-15");

		VoucherAutoSaveRequestDto voucherAutoSaveRequestDto = VoucherAutoSaveRequestDto.builder()
				.texts(texts)
				.filename("1623777600000_스타벅스_아이스아메리카노T.png")
				.build();
		GptResponseDto gptResponseDto = GptResponseDto.builder()
				.choices(new ArrayList<>())
				.build();
		VoucherSaveRequestDto voucherSaveRequestDto = VoucherSaveRequestDto.builder()
				.brandName("스타벅스")
				.productName("아이스 아메리카노 T")
				.barcode("012345678910")
				.expiresAt("2023-06-15")
				.build();
		SearchResponseDto searchResponseDto = SearchResponseDto.builder()
				.hits(new SearchResponseDto.Hits())
				.build();
		VoucherSaveResponseDto voucherSaveResponseDto = VoucherSaveResponseDto.builder()
				.id(1L)
				.build();

		VoucherAutoSaveRequestDto mockVoucherAutoSaveRequestDto = new VoucherAutoSaveRequestDto(); // You might want to set some properties if needed
		String mockUsername = "testUser";

		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(gptService.getGptResponse(any(VoucherAutoSaveRequestDto.class))).thenReturn(Mono.just(gptResponseDto));
		when(searchService.search(anyString())).thenReturn(Mono.just(searchResponseDto));
		when(voucherService.save(anyString(), any(VoucherSaveRequestDto.class))).thenReturn(voucherSaveResponseDto);

		// When
		voucherSaveService.execute(mockVoucherAutoSaveRequestDto, mockUsername, 1L);

		// Then
		mockMvc.perform(post("/vouchers")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucherAutoSaveRequestDto)))
				.andExpect(status().isOk());
	}

	/**
	 * 기프티콘 공유 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void shareVoucher() throws Exception {
		//Given
		Long voucherId = 1L;
		VoucherShareRequestDto voucherShareRequestDto = VoucherShareRequestDto.builder()
				.message("축하드립니다")
				.build();
		VoucherShareResponseDto voucherShareResponseDto = VoucherShareResponseDto.builder()
				.id("uuid")
				.build();
		//When
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.share(anyString(), eq(voucherId), any(VoucherShareRequestDto.class)))
				.thenReturn(voucherShareResponseDto);

		//Then
		mockMvc.perform(post("/vouchers/1/share")
						.header("Authorization", "my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(voucherShareRequestDto)))
				.andExpect(status().isOk());
	}

	/**
	 * 기프티콘 공유 취소 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void shareCancel() throws Exception {
		//Given
		Long voucherId = 1L;
		VoucherShareRequestDto voucherShareRequestDto = VoucherShareRequestDto.builder()
				.message("축하드립니다")
				.build();
		VoucherShareResponseDto voucherShareResponseDto = VoucherShareResponseDto.builder()
				.id("uuid")
				.build();
		Giftcard giftcard = Giftcard.builder()
				.id("uuid")
				.build();
		//When
		when(jwtProvider.resolveToken(any())).thenReturn("my_awesome_access_token");
		when(jwtProvider.getUsername(anyString())).thenReturn("이진우");
		when(voucherService.cancelShare(anyString(), eq(voucherId))).thenReturn(giftcard);

		//Then
		mockMvc.perform(delete("/vouchers/1/share")
						.header("Authorization", "my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON))
				.andExpect(status().isOk());
	}
}