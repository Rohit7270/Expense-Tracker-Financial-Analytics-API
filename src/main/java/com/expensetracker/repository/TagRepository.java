package com.expensetracker.repository;

import com.expensetracker.entity.Tag;
import com.expensetracker.entity.User;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface TagRepository extends JpaRepository<Tag, Long> {
    List<Tag> findByUser(User user);
    Optional<Tag> findByIdAndUser(Long id, User user);
    Optional<Tag> findByNameIgnoreCaseAndUser(String name, User user);
}
