package org.swmaestro.repl.gifthub.vouchers.repository;

import java.util.Optional;

import org.springframework.data.jpa.repository.JpaRepository;
import org.swmaestro.repl.gifthub.vouchers.entity.Product;

public interface ProductRepository extends JpaRepository<Product, Long> {
	Product findByName(String productName);

	Optional<Product> findByBrandIdAndName(Long brandId, String name);
}
