package org.swmaestro.repl.gifthub.giftcard.contorller;

import static org.mockito.Mockito.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

import java.time.LocalDate;

import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.web.servlet.MockMvc;
import org.swmaestro.repl.gifthub.giftcard.dto.GiftcardResponseDto;
import org.swmaestro.repl.gifthub.giftcard.service.GiftcardService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
class GiftcardControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private GiftcardService giftcardService;

	@Test
	@WithMockUser(username = "test", roles = "USER")
	void read() throws Exception {
		// given
		String giftcardId = "id";
		String apiPath = "/giftcards/" + giftcardId;
		String encodedPassword = "MDAwMA==";
		String decodedPassword = "0000";

		GiftcardResponseDto giftcardResponseDto = GiftcardResponseDto.builder()
				.sender("보내는 사람")
				.message("메시지")
				.productName("상품명")
				.brandName("브랜드명")
				.expiresAt(LocalDate.now())
				.build();

		// when
		when(giftcardService.read(giftcardId, decodedPassword)).thenReturn(giftcardResponseDto);

		// then
		mockMvc.perform(get(apiPath)
						.contentType(MediaType.APPLICATION_JSON)
						.header("Authorization", "Basic " + encodedPassword)
						.content(objectMapper.writeValueAsString("test")))
				.andExpect(status().isOk())
				.andExpect(jsonPath("$.path").value(apiPath))
				.andExpect(jsonPath("$.data.sender").value(giftcardResponseDto.getSender()))
				.andExpect(jsonPath("$.data.message").value(giftcardResponseDto.getMessage()))
				.andExpect(jsonPath("$.data.product_name").value(giftcardResponseDto.getProductName()))
				.andExpect(jsonPath("$.data.brand_name").value(giftcardResponseDto.getBrandName()))
				.andExpect(jsonPath("$.data.expires_at").value(giftcardResponseDto.getExpiresAt().toString()));
	}
}