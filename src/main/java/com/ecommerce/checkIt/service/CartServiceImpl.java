package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.exceptions.APIException;
import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.Cart;
import com.ecommerce.checkIt.model.CartItem;
import com.ecommerce.checkIt.model.Product;
import com.ecommerce.checkIt.payload.CartDTO;
import com.ecommerce.checkIt.payload.CartItemDTO;
import com.ecommerce.checkIt.payload.ProductDTO;
import com.ecommerce.checkIt.repositories.CartItemRepository;
import com.ecommerce.checkIt.repositories.CartRepository;
import com.ecommerce.checkIt.repositories.ProductRepository;
import com.ecommerce.checkIt.utils.AuthUtil;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Service
public class CartServiceImpl implements CartService{

    @Autowired
    ModelMapper modelMapper;

    @Autowired
    CartRepository cartRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    CartItemRepository cartItemRepository;

    @Autowired
    AuthUtil authUtil;

    @Override
    public CartDTO addProductToCart(Long productId, Integer quantity) {
        //Find existing cart or create one
        Cart cart = createCart();
        //Retrieve Product Details
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productId));
        //Perform Validations
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(
                cart.getCartId(),
                productId
        );
        if(cartItem != null) {
            throw new APIException("Product " + product.getProductName() + " already exists in the cart");
        }
        if(product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + " is not available");
        }
        if(product.getQuantity() < quantity) {
            throw new APIException("Please, make an order of the " + product.getProductName() + " less than or equal to the quantity" + product.getQuantity() + ".");
        }
        //Create Cart Item
        CartItem newCartItem = new CartItem();
        newCartItem.setProduct(product);
        newCartItem.setCart(cart);
        newCartItem.setQuantity(quantity);
        newCartItem.setDiscount(product.getDiscount());
        newCartItem.setProductPrice(product.getSpecialPrice());
        //Save Cart Item
        cartItemRepository.save(newCartItem);
        product.setQuantity(product.getQuantity());
        cart.setTotalPrice(cart.getTotalPrice() + (product.getSpecialPrice() * quantity));
        cartRepository.save(cart);

        //Return updated cart
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItemList = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItemList.stream().map(item -> {
            ProductDTO map = modelMapper.map(item.getProduct(), ProductDTO.class);
            map.setQuantity(item.getQuantity());
            return map;
        });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Override
    public List<CartDTO> getAllCarts() {
        List<Cart> cartsList = cartRepository.findAll();
        if(cartsList.isEmpty()) {
            throw new APIException("No Cart exists");
        }
        return cartsList.stream().map((cart) -> {
            CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
            List<ProductDTO> productDTOS = cart.getCartItems().stream().map(cartItem -> {
                ProductDTO productDTO = modelMapper.map(cartItem.getProduct(),ProductDTO.class);
                productDTO.setQuantity(cartItem.getQuantity());
                return productDTO;
            }).toList();
            cartDTO.setProducts(productDTOS);
            return cartDTO;
        }).toList();
    }

    @Override
    public CartDTO getCart(String emailId, Long cartId) {
        Cart cart = cartRepository.findCartByEmailAndCartId(emailId, cartId);
        if(cart == null) {
            throw new ResourceNotFoundException("Cart", "CartId", cartId);
        }
        CartDTO cartDTOS = modelMapper.map(cart, CartDTO.class);
        cart.getCartItems().forEach(c -> c.getProduct().setQuantity(c.getQuantity()));
        List<ProductDTO> products = cart.getCartItems().stream().map(p -> modelMapper.map(p.getProduct(), ProductDTO.class)).toList();
        cartDTOS.setProducts(products);
        return cartDTOS;
    }

    @Override
    @Transactional
    public CartDTO updateProductQuantity(Long productId, Integer updateStatus) {
        String emailId = authUtil.loggedInEmail();
        Cart userCart = cartRepository.findCartByEmail(emailId);
        Long cartId = userCart.getCartId();
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "CartId", cartId));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        if(product.getQuantity() == 0) {
            throw new APIException(product.getProductName() + "is not available in cart...");
        }
        if(product.getQuantity() < updateStatus) {
            throw new APIException("Please, make an order of the " + product.getProductName() + " less than or equal to the quantity" + product.getQuantity() + ".");
        }
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart....");
        }
        int newQuantity = cartItem.getQuantity() + updateStatus;
        if(newQuantity < 0) {
            throw new APIException("The resulting quanity cannot be negative.");
        }
        if(newQuantity == 0) {
            deleteProductFromCart(cartId, productId);
        }else {
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setQuantity(cartItem.getQuantity() + updateStatus);
            cartItem.setDiscount(product.getDiscount());
            cart.setTotalPrice(cart.getTotalPrice() + (cartItem.getProductPrice() * updateStatus));
            cartRepository.save(cart);
        }
        CartItem updatedItem = cartItemRepository.save(cartItem);
        if(updatedItem.getQuantity() == 0) {
            cartItemRepository.deleteById(updatedItem.getCartItemId());
        }
        CartDTO cartDTO = modelMapper.map(cart, CartDTO.class);
        List<CartItem> cartItems = cart.getCartItems();
        Stream<ProductDTO> productDTOStream = cartItems.stream().map(itm -> {
            ProductDTO productDTO = modelMapper.map(itm.getProduct(), ProductDTO.class);
            productDTO.setQuantity(itm.getQuantity());
            return productDTO;
        });
        cartDTO.setProducts(productDTOStream.toList());
        return cartDTO;
    }

    @Transactional
    @Override
    public String deleteProductFromCart(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "cartId", cartId));
        CartItem cartItem = cartItemRepository.findByProductId(productId);
        if(cartItem == null) {
            throw new ResourceNotFoundException("Product", "ProductId", productId);
        }
        cart.setTotalPrice(cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItemRepository.deleteCartItemByProductIdAndCartId(cartId, productId);
        return "Product" + cartItem.getProduct().getProductName() + " removed from the cart !!!";
    }

    @Override
    public void updateProductInCarts(Long cartId, Long productId) {
        Cart cart = cartRepository.findById(cartId).orElseThrow(() -> new ResourceNotFoundException("Cart", "CartId", cartId));
        Product product = productRepository.findById(productId).orElseThrow(() -> new ResourceNotFoundException("Product", "ProductId", productId));
        CartItem cartItem = cartItemRepository.findCartItemByProductIdAndCartId(cartId, productId);
        if(cartItem == null) {
            throw new APIException("Product " + product.getProductName() + " not available in the cart...");
        }

        double cartPrice = cart.getTotalPrice() - (cartItem.getProductPrice() * cartItem.getQuantity());
        cartItem.setProductPrice(product.getSpecialPrice());
        cart.setTotalPrice(cartPrice + (cartItem.getProductPrice() * cartItem.getQuantity()));
        cartItem = cartItemRepository.save(cartItem);


    }

    @Transactional
    @Override
    public String createOrUpdateCartWithItems(List<CartItemDTO> cartItems) {
        //Get User's email
        String emailId = authUtil.loggedInEmail();

        Cart existingCart = cartRepository.findCartByEmail(emailId);
        if(existingCart == null) {
            //check if an existing cart is avaiable or create new one
            existingCart = new Cart();
            existingCart.setTotalPrice(0.00);
            existingCart.setUser(authUtil.loggedInUser());
            existingCart = cartRepository.save(existingCart);
        } else {
            //clear all current items in the existing cart
            cartItemRepository.deleteAllByCartId(existingCart.getCartId());

        }

        double totalPrice = 0.0;

        //Process each Item in the request to add the cart
        for(CartItemDTO cartItemDTO: cartItems) {
            //find the product by ID
            Long productID = cartItemDTO.getProductId();
            Integer quantity = cartItemDTO.getQuantity();

            Product product = productRepository.findById(productID).orElseThrow(() -> new ResourceNotFoundException("Product", "productId", productID));
            //Directly update the product stock and total price
            //product.setQuantity(product.getQuantity() - quantity);
            totalPrice += product.getSpecialPrice() * quantity;

            //create and save cart item
            CartItem cartItem = new CartItem();
            cartItem.setProduct(product);
            cartItem.setCart(existingCart);
            cartItem.setQuantity(quantity);
            cartItem.setProductPrice(product.getSpecialPrice());
            cartItem.setDiscount(product.getDiscount());
            cartItemRepository.save(cartItem);
        }

        existingCart.setTotalPrice(totalPrice);
        cartRepository.save(existingCart);
        return "Cart Created and Updated with new Items Successfully";
    }

    private Cart createCart() {
        Cart userCart = cartRepository.findCartByEmail(authUtil.loggedInEmail());
        if(userCart != null) return userCart;

        Cart cart = new Cart();
        cart.setTotalPrice(0.00);
        cart.setUser(authUtil.loggedInUser());
        return cartRepository.save(cart);
    }
}
