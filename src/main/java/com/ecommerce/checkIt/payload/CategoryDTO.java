package com.ecommerce.checkIt.payload;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.RequiredArgsConstructor;

@Data
@AllArgsConstructor
@RequiredArgsConstructor
public class CategoryDTO {
    private Long categoryId;
    @NotBlank(message = "Category must not be null.")
    @Size(min = 5, message = "Category name must be at least 5 characters long.")
    private String categoryName;
}
