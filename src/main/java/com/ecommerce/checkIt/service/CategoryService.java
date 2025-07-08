package com.ecommerce.checkIt.service;


import com.ecommerce.checkIt.model.Category;
import com.ecommerce.checkIt.payload.CategoryDTO;
import com.ecommerce.checkIt.payload.CategoryResponse;

import java.util.List;

public interface CategoryService {
    CategoryResponse getAllCategories(Integer pageNumber, Integer pageSize, String sortBy, String sortOrder);
    CategoryDTO  createCategory(CategoryDTO category);
    CategoryDTO deleteCategory(Long categoryId);
    CategoryDTO updateCategory(CategoryDTO category, Long categoryId);
}
