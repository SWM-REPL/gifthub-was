package org.swmaestro.repl.gifthub.vouchers.service;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.repository.BrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {
	private final BrandRepository brandRepository;

	public Brand read(String brandName) {
		return brandRepository.findByName(brandName);
	}
}
