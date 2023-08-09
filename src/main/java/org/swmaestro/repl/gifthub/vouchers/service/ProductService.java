package org.swmaestro.repl.gifthub.vouchers.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.dto.ProductReadResponseDto;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;
import org.swmaestro.repl.gifthub.vouchers.repository.ProductRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class ProductService {
	private final ProductRepository productRepository;

	public Product read(String productName) {
		return productRepository.findByName(productName);
	}

	public ProductReadResponseDto readById(Long id) {
		Optional<Product> product = productRepository.findById(id);
		if (product.isEmpty()) {
			throw new BusinessException("존재하지 않는 상품 입니다.", StatusEnum.NOT_FOUND);
		}
		ProductReadResponseDto productReadResponseDto = mapToDto(product.get());
		return productReadResponseDto;
	}

	public Product save(String productName, Brand brand) {
		Product product = Product.builder()
				.name(productName)
				.brand(brand)
				.build();

		return productRepository.save(product);
	}

	public ProductReadResponseDto mapToDto(Product product) {
		ProductReadResponseDto productReadResponseDto = ProductReadResponseDto.builder()
				.id(product.getId())
				.brandId(product.getBrand().getId())
				.name(product.getName())
				.description(product.getDescription())
				.isReusable(product.getIsReusable())
				.price(product.getPrice())
				.imageUrl(product.getImageUrl())
				.build();
		return productReadResponseDto;
	}
}
