package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.payload.CartDTO;
import com.ecommerce.checkIt.payload.CartItemDTO;
import jakarta.transaction.Transactional;
import org.springframework.stereotype.Service;

import java.util.List;


public interface CartService {
    public CartDTO addProductToCart(Long productId, Integer quantity);

    List<CartDTO> getAllCarts();

    CartDTO getCart(String emailId, Long cartId);
    @Transactional
    CartDTO updateProductQuantity(Long productId, Integer updateStatus);

    String deleteProductFromCart(Long cartId, Long productId);

    void updateProductInCarts(Long cartId, Long productId);

    String createOrUpdateCartWithItems(List<CartItemDTO> cartItems);
}
