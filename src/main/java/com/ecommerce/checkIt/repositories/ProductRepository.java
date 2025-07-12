package com.ecommerce.checkIt.repositories;

import com.ecommerce.checkIt.model.Category;
import com.ecommerce.checkIt.model.Product;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;


@Repository
public interface ProductRepository extends JpaRepository<Product, Long> {
    Page<Product> findByCategoryOrderByPriceAsc(Category category, Pageable pageable);

    Page<Product> findByProductNameLikeIgnoreCase(String keyword, Pageable pageable);

    Optional<Product> findByProductName(@NotBlank(message = "productName must not be blank") @NotNull(message = "productName must not be null") @Size(min = 3) String productName);
}
