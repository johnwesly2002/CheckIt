package com.ecommerce.checkIt.repositories;

import com.ecommerce.checkIt.model.Category;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {


    Category findByCategoryName(@NotBlank @Size(min = 5, message = "Category name must be at least 5 characters long") String categoryName);
}
