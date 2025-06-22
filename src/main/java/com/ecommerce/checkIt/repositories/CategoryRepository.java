package com.ecommerce.checkIt.repositories;

import com.ecommerce.checkIt.model.Category;
import org.springframework.data.jpa.repository.JpaRepository;

public interface CategoryRepository extends JpaRepository<Category, Long> {


}
