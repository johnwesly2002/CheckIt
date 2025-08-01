package com.ecommerce.checkIt.repositories;

import com.ecommerce.checkIt.model.Address;
import com.ecommerce.checkIt.model.User;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AddressRepository extends JpaRepository<Address, Long> {
}
