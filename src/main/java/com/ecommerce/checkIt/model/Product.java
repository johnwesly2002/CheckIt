package com.ecommerce.checkIt.model;


import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Entity
@Data
@AllArgsConstructor
@NoArgsConstructor
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long productId;

    @NotBlank(message = "productName must not be blank")
    @Size(min = 3, message = "Product name must contain at least 3 characters")
    private String productName;

    private String image;

    @NotBlank(message = "productDescription must not be blank")
    @Size(min = 6, message = "Product name must contain at least 6 characters")
    private String productDescription;

    @NotNull(message = "quantity must not be null")
    private Integer quantity;

    @NotNull(message = "price must not be null")
    private double price; //100

    private double specialPrice; //75 (100 - (25 / 100) * 100);

    private double discount; // 25

    @ManyToOne
    @JoinColumn(name = "category_id")
    private Category category;
}
