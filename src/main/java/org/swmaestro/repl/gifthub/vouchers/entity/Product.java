package org.swmaestro.repl.gifthub.vouchers.entity;

import org.hibernate.annotations.ColumnDefault;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class Product {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;

	@ManyToOne
	@JoinColumn(name = "brand_id", nullable = false)
	private Brand brand;

	@Column(length = 100, nullable = false)
	private String name;

	@Column(columnDefinition = "TEXT")
	private String description;

	@Column(columnDefinition = "TINYINT", nullable = false)
	@ColumnDefault("0")
	private int isReusable;

	@Column
	private Integer price;

	@Column(length = 200)
	private String imageUrl;

	@Builder
	public Product(Long id, Brand brand, String name, String description, int isReusable, Integer price, String imageUrl) {
		this.id = id;
		this.brand = brand;
		this.name = name;
		this.description = description;
		this.isReusable = isReusable;
		this.price = price;
		this.imageUrl = imageUrl;
	}
}
