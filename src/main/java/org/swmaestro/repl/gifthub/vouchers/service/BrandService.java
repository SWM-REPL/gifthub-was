package org.swmaestro.repl.gifthub.vouchers.service;

import java.util.Optional;

import org.springframework.stereotype.Service;
import org.swmaestro.repl.gifthub.exception.BusinessException;
import org.swmaestro.repl.gifthub.util.StatusEnum;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;
import org.swmaestro.repl.gifthub.vouchers.repository.BrandRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class BrandService {
	private final BrandRepository brandRepository;

	public Optional<Brand> read(String brandName) {
		return brandRepository.findByName(brandName);
	}

	public Brand readById(Long id) {
		Optional<Brand> brand = brandRepository.findById(id);
		if (brand.isEmpty()) {
			throw new BusinessException("존재하지 않는 브랜드 입니다.", StatusEnum.NOT_FOUND);
		}
		return brand.get();
	}

	public Brand save(String brandName) {
		Brand brand = Brand.builder()
				.name(brandName)
				.build();

		return brandRepository.save(brand);
	}
}
