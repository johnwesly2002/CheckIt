package com.ecommerce.checkIt.service;

import com.ecommerce.checkIt.payload.StripePaymentDTO;
import com.stripe.Stripe;
import com.stripe.exception.StripeException;
import com.stripe.model.Customer;
import com.stripe.model.CustomerSearchResult;
import com.stripe.model.PaymentIntent;
import com.stripe.param.ChargeSearchParams;
import com.stripe.param.CustomerCreateParams;
import com.stripe.param.CustomerSearchParams;
import com.stripe.param.PaymentIntentCreateParams;
import jakarta.annotation.PostConstruct;
import jakarta.transaction.Transactional;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

@Service
@Transactional
public class StripeServiceImpl implements StripeService{

    @Value("${stripe.secret.key}")
    private String stripeapiKey;

    @PostConstruct
    public void init() {
        Stripe.apiKey = stripeapiKey;
    }


    @Override
    public PaymentIntent paymentIntent(StripePaymentDTO stripePaymentDTO) throws StripeException {
        Customer customer;
        //retrieve and check if customer check
        CustomerSearchParams Searchparams =
                CustomerSearchParams.builder().setQuery("email:'"+stripePaymentDTO.getEmail()+"'").build();
        CustomerSearchResult customers = Customer.search(Searchparams);
        if(customers.getData().isEmpty()) {
            //create new customer
           CustomerCreateParams customerParams =   CustomerCreateParams.builder()
                    .setName(stripePaymentDTO.getName())
                    .setEmail(stripePaymentDTO.getEmail())
                    .setAddress(
                            CustomerCreateParams.Address.builder()
                                    .setLine1(stripePaymentDTO.getAddress().getStreet())
                                    .setCity(stripePaymentDTO.getAddress().getCity())
                                    .setState(stripePaymentDTO.getAddress().getState())
                                    .setPostalCode(stripePaymentDTO.getAddress().getPincode())
                                    .setCountry(stripePaymentDTO.getAddress().getCountry())
                                    .build()
                    ).build();
           customer = Customer.create(customerParams);
        }else {
            customer = customers.getData().get(0);
        }
        PaymentIntentCreateParams params =
                PaymentIntentCreateParams.builder()
                        .setAmount(stripePaymentDTO.getAmount())
                        .setCurrency(stripePaymentDTO.getCurrency())
                        .setCustomer(customer.getId())
                        .setDescription(stripePaymentDTO.getDescription())
                        .build();

        return PaymentIntent.create(params);
    }
}
