package com.ecommerce.checkIt.model;

import jakarta.persistence.*;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.ArrayList;
import java.util.List;
import java.util.Set;

@Entity
@Table(name = "addresses")
@Data
@NoArgsConstructor
@AllArgsConstructor
public class Address {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long addressId;
    @NotBlank
    @Size(min = 5, message = "Street name must be at least 5 characters")
    private String street;

    @NotBlank
    @Size(min = 5, message = "Building name must be atleast 5 characters")
    private String buildingName;

    @NotBlank
    @Size(min = 4, message = "City name must be atleast 4 characters")
    private String City;

    @NotBlank
    @Size(min = 2, message = "State name must be atleast 2 characters")
    private String State;

    @NotBlank
    @Size(min = 2, message = "Country name must be atleast 2 characters")
    private String Country;

    @NotBlank
    @Size(min = 6, message = "pincode name must be atleast 6 characters")
    private String pincode;

    @ManyToOne
    @JoinColumn(name="user_id")
    private User user;

    public Address(String street, String buildingName, String city, String state, String country, String pincode) {
        this.street = street;
        this.buildingName = buildingName;
        City = city;
        State = state;
        Country = country;
        this.pincode = pincode;
    }
}
