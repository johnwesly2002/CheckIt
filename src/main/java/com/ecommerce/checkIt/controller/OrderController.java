package com.ecommerce.checkIt.controller;

import com.ecommerce.checkIt.payload.OrderDTO;
import com.ecommerce.checkIt.payload.OrderRequestDTO;
import com.ecommerce.checkIt.payload.StripePaymentDTO;
import com.ecommerce.checkIt.service.OrderService;
import com.ecommerce.checkIt.service.StripeService;
import com.ecommerce.checkIt.utils.AuthUtil;
import com.stripe.exception.StripeException;
import com.stripe.model.PaymentIntent;
import jakarta.validation.Valid;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api")
public class OrderController {

    @Autowired
    private OrderService orderService;

    @Autowired
    AuthUtil authUtil;

    @Autowired
    StripeService stripeService;

    @PostMapping("/order/users/payments/{paymentMethod}")
    public ResponseEntity<OrderDTO> orderProducts(@PathVariable String paymentMethod, @Valid @RequestBody OrderRequestDTO orderRequestDTO) {
        String emailId = authUtil.loggedInEmail();
        OrderDTO orderDTO = orderService.placeOrder(
                emailId,
                orderRequestDTO.getAddressId(),
                paymentMethod,
                orderRequestDTO.getPgName(),
                orderRequestDTO.getPgPaymentId(),
                orderRequestDTO.getPgStatus(),
                orderRequestDTO.getPgResponseMessage()
        );
        return new ResponseEntity<>(orderDTO, HttpStatus.OK);
    }
    @PostMapping("/order/stripe-client-secret")
    public ResponseEntity<String> orderProducts(@RequestBody StripePaymentDTO stripePaymentDTO) throws StripeException {
        System.out.println("Stripe payment information recevied" + stripePaymentDTO);
        PaymentIntent paymentIntent = stripeService.paymentIntent(stripePaymentDTO);
        return new ResponseEntity<>(paymentIntent.getClientSecret(), HttpStatus.CREATED);
    }
    }
