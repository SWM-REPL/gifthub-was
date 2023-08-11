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
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.service.BrandService;

import com.fasterxml.jackson.databind.ObjectMapper;

@SpringBootTest
@AutoConfigureMockMvc
public class BrandControllerTest {
	@Autowired
	private MockMvc mockMvc;

	@Autowired
	private ObjectMapper objectMapper;

	@MockBean
	private BrandService brandService;

	/**
	 * 브랜드 상세 조회 테스트
	 */
	@Test
	@WithMockUser(username = "이진우", roles = "USER")
	void readBrand() throws Exception {
		// given
		Brand brand = Brand.builder()
				.id(1L)
				.name("스타벅스")
				.imageUrl("image_url_1")
				.build();
		// when
		when(brandService.readById(1L)).thenReturn(brand);
		// then
		mockMvc.perform(get("/brands/1")
						.header("Authorization", "Bearer my_awesome_access_token")
						.contentType(MediaType.APPLICATION_JSON)
						.content(objectMapper.writeValueAsString(brand)))
				.andExpect(status().isOk());
	}
}
