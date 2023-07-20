package org.swmaestro.repl.gifthub.vouchers.repository;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
	Brand findByBrandName(String brandName);
}
