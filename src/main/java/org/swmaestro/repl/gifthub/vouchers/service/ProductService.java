package org.swmaestro.repl.gifthub.vouchers.service;

import org.springframework.stereotype.Service;
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
}
