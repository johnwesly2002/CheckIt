package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.exceptions.APIException;
import com.ecommerce.checkIt.exceptions.ResourceNotFoundException;
import com.ecommerce.checkIt.model.*;
import com.ecommerce.checkIt.payload.OrderDTO;
import com.ecommerce.checkIt.payload.OrderItemDTO;
import com.ecommerce.checkIt.repositories.*;
import jakarta.transaction.Transactional;
import org.modelmapper.ModelMapper;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;

@Service
public class OrderServiceImpl implements OrderService {

    @Autowired
    CartRepository cartRepository;

    @Autowired
    AddressRepository addressRepository;

    @Autowired
    PaymentRepository paymentRepository;

    @Autowired
    OrderRepository orderRepository;

    @Autowired
    OrderItemRepository orderItemRepository;

    @Autowired
    ProductRepository productRepository;

    @Autowired
    private ModelMapper modelMapper;

    @Autowired
    CartService cartService;

    @Override
    @Transactional
    public OrderDTO placeOrder(String emailId, Long addressId, String paymentMethod, String pgName, String pgPaymentId, String pgStatus, String pgResponseMessage) {
        //Getting user cart
        Cart cart = cartRepository.findCartByEmail(emailId);
        if(cart == null) {
            throw new APIException("Cart not found for emailId" + emailId);
        }
        Address address = addressRepository.findById(addressId).orElseThrow(() -> new ResourceNotFoundException("Address", "addressId", addressId));
        //Create a new Order with payment info
        Order order = new Order();
        order.setEmail(emailId);
        order.setOrderDate(LocalDate.now());
        order.setTotalAmount(cart.getTotalPrice());
        order.setOrderStatus("Order Accepted !");
        order.setAddress(address);
        Payment payment = new Payment(paymentMethod, pgPaymentId, pgName, pgStatus, pgResponseMessage);
        payment.setOrder(order);
        payment = paymentRepository.save(payment);
        order.setPayment(payment);

        Order savedOrder = orderRepository.save(order);
        // get items from the cart into the order items
        List<CartItem> cartItemList = cart.getCartItems();
        if(cartItemList.isEmpty()) {
            throw new APIException("Cart is Empty to take Order...");
        }
        List<OrderItem> orderItems = new ArrayList<>();
        for(CartItem cartItem: cartItemList) {
            OrderItem orderItem = new OrderItem();
            orderItem.setProduct(cartItem.getProduct());
            orderItem.setQuantity(cartItem.getQuantity());
            orderItem.setDiscount(cartItem.getDiscount());
            orderItem.setOrderedProductPrice(cartItem.getProductPrice());
            orderItem.setOrder(savedOrder);
            orderItems.add(orderItem);
        }

        orderItems = orderItemRepository.saveAll(orderItems);
        //Post Order methods to do....
        //update product stock
        cart.getCartItems().forEach(itm -> {
            int quantity = itm.getQuantity();
            Product product = itm.getProduct();
            product.setQuantity(product.getQuantity() - quantity);
            productRepository.save(product);

            //clear cart
            cartService.deleteProductFromCart(cart.getCartId(),itm.getProduct().getProductId());
        });


        //send back the order summary
        OrderDTO orderDTO = modelMapper.map(order, OrderDTO.class);
        orderItems.forEach(itm -> orderDTO.getOrderItems().add(modelMapper.map(itm, OrderItemDTO.class)));
        orderDTO.setAddressId(addressId);
        return orderDTO;
    }
}
