package br.com.senior.transport_logistics.domain.product;

import br.com.senior.transport_logistics.domain.product.enums.ProductCategory;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface ProductRepository extends JpaRepository<ProductEntity,Long> {

    @Query("""
            SELECT p FROM Product p
            WHERE (:category IS NULL OR p.category = :category)
            AND (:limitWeight IS NULL OR p.weight <= :limitWeight)
            """)
    Page<ProductEntity> findAllProductsWithFilters(
            @Param(value = "category") ProductCategory category,
            @Param(value = "limitWeight") Float limitWeight,
            Pageable pageable
    );

    boolean existsByNameIgnoreCase(String name);
}
