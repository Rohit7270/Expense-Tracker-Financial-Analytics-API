package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.ERole;
import com.enterprise.expensetracker.entity.Role;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface RoleRepository extends JpaRepository<Role, Long> {
    Optional<Role> findByName(ERole name);
}
