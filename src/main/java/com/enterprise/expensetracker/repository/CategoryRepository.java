package com.enterprise.expensetracker.repository;

import com.enterprise.expensetracker.entity.Category;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface CategoryRepository extends JpaRepository<Category, Long> {
    
    @Query("SELECT c FROM Category c WHERE c.user IS NULL OR c.user.id = :userId")
    List<Category> findByUserIdOrUserIsNull(@Param("userId") Long userId);
    
    Boolean existsByNameIgnoreCaseAndUserIsNull(String name);
    Boolean existsByNameIgnoreCaseAndUserId(String name, Long userId);
}
