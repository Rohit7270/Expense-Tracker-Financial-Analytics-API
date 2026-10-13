package com.expensetracker.repository;

import com.expensetracker.entity.Category;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface CategoryRepository extends JpaRepository<Category, Long> {

    // Global (user is null) categories + this user's own categories
    List<Category> findByUserIsNullOrUser(User user);

    Optional<Category> findByIdAndUser(Long id, User user);

    Optional<Category> findByIdAndUserIsNull(Long id);
}
