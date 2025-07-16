package com.ecommerce.checkIt.repositories;

import com.ecommerce.checkIt.model.AppRole;
import com.ecommerce.checkIt.model.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Integer> {

    Optional<Role> findByRoleName(AppRole appRole);
}
