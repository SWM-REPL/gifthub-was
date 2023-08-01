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
import org.swmaestro.repl.gifthub.vouchers.dto.ProductReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.service.ProductService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class ProductControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private ProductService productService;

	/**
	 * 상품 상세 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readProduct() throws Exception {
		// given
		ProductReadResponseDto productReadResponseDto = ProductReadResponseDto.builder()
				.id(1L)
				.brandId(1L)
				.name("아이스 아메리카노")
				.price(4500)
				.imageUrl("https://스타벅스_아이스아메리카노T.png")
				.build();
		// when
		when(productService.readById(1L)).thenReturn(productReadResponseDto);
		// then
		mockMvc.perform(get("/products/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(productReadResponseDto)))
				.andExpect(status().isOk());
	}

}
