package org.swmaestro.repl.gifthub.vouchers.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Brand;

public interface BrandRepository extends JpaRepository<Brand, Long> {
	Optional<Brand> findByName(String brandName);
}
