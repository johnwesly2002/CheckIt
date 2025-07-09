package com.ecommerce.checkIt.service;


import com.ecommerce.checkIt.payload.ProductDTO;

public interface ProductService {
    ProductDTO addProduct(Long categoryId, ProductDTO product);
}
