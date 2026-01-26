package com.hexamarket.code.entity;

import org.hibernate.annotations.SQLDelete;
import org.hibernate.annotations.SQLRestriction;

import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import jakarta.persistence.Version;
import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

@Entity
@Table(name = "inventory")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
// Soft delete cho Inventory
@SQLDelete(sql = "UPDATE inventory SET is_deleted = true WHERE id = ?")
@SQLRestriction("is_deleted = false")
public class Inventory {
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	@OneToOne(fetch = FetchType.LAZY)
	@JoinColumn(name = "variant_id", nullable = false, unique = true)
	private ProductVariant variant;
	private Integer quantity;
	private Integer reservedQuantity;
	@Version
	private Long version;
	// Thêm is_deleted column
	@Column(name = "is_deleted")
	private boolean deleted = false;
}
