package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.model.Category;
import org.springframework.http.HttpStatus;
import org.springframework.http.HttpStatusCode;
import org.springframework.stereotype.Service;
import org.springframework.web.bind.annotation.DeleteMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.server.ResponseStatusException;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class CategoryServiceImpl implements CategoryService {
    private final List<Category> categories = new ArrayList<>();
    private Long newId = 1L;
    @Override
    public List<Category> getAllCategories() {
        return categories;
    }

    @Override
    public void createCategory(@RequestBody Category category){
        category.setCategoryId(newId++);
        categories.add(category);
    }

    @Override
    public String deleteCategory(Long categoryId){
        Category category = categories
                .stream()
                .filter(c -> c != null && categoryId.equals(c.getCategoryId()))
                .findFirst().orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "Category Not Found"));
        if(category == null) return "Category Not Found";
        categories.remove(category);
        return "Category with categoryId: "+ categoryId + " deleted successfully";
    }

    @Override
    public Category updateCategory(Category category, Long categoryId) {
        Optional<Category> category1 = categories.stream()
                .filter(c -> c != null && categoryId.equals(c.getCategoryId()))
                .findFirst();
        if(category1.isPresent()){
            Category existingCategory = category1.get();
            existingCategory.setCategoryName(category.getCategoryName());
            return existingCategory;
        }else {
            throw new ResponseStatusException(HttpStatus.NOT_FOUND, "Category Not Found");
        }
    }
}
