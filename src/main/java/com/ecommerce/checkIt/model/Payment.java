package com.ecommerce.checkIt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "payments")
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Payment {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long paymentId;

    @OneToOne(mappedBy = "payment", cascade = {CascadeType.PERSIST, CascadeType.MERGE})
    private Order order;

    @NotBlank
    @Size(min = 4, message = "Payment method must contain at least 4 characters")
    private String paymentMethod;

    private String pgPaymentId;

    private String pgStatus;

    private String pgResponseMessage;

    private String pgName;


    public Payment(String paymentMethod, String pgPaymentId, String pgName, String pgStatus, String pgResponseMessage) {
        this.paymentMethod = paymentMethod;
        this.pgPaymentId = pgPaymentId;
        this.pgName = pgName;
        this.pgStatus = pgStatus;
        this.pgResponseMessage = pgResponseMessage;
    }
}
