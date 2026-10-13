package com.expensetracker.repository;

import com.expensetracker.entity.Notification;
import com.expensetracker.entity.User;
import com.expensetracker.enums.NotificationType;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface NotificationRepository extends JpaRepository<Notification, Long> {
    List<Notification> findByUserOrderByCreatedAtDesc(User user);
    Optional<Notification> findByIdAndUser(Long id, User user);
    boolean existsByUserAndTypeAndMessage(User user, NotificationType type, String message);
}
