package com.expensetracker.service;

import com.expensetracker.dto.response.NotificationResponse;
import com.expensetracker.entity.Notification;
import com.expensetracker.entity.User;
import com.expensetracker.enums.NotificationType;
import com.expensetracker.exception.ResourceNotFoundException;
import com.expensetracker.mapper.NotificationMapper;
import com.expensetracker.repository.NotificationRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Service
@RequiredArgsConstructor
public class NotificationService {

    private final NotificationRepository notificationRepository;
    private final SecurityUtils securityUtils;
    private final NotificationMapper notificationMapper;

    @Transactional
    public void createNotification(User user, String message, NotificationType type) {
        Notification notification = Notification.builder()
                .user(user)
                .message(message)
                .type(type)
                .isRead(false)
                .build();
        notificationRepository.save(notification);
    }

    @Transactional(readOnly = true)
    public boolean hasNotification(User user, String message, NotificationType type) {
        return notificationRepository.existsByUserAndTypeAndMessage(user, type, message);
    }

    @Transactional(readOnly = true)
    public List<NotificationResponse> getAllNotifications() {
        User user = securityUtils.getCurrentUser();
        return notificationRepository.findByUserOrderByCreatedAtDesc(user).stream()
                .map(notificationMapper::toResponse)
                .toList();
    }

    @Transactional
    public NotificationResponse markAsRead(Long id) {
        User user = securityUtils.getCurrentUser();
        Notification notification = notificationRepository.findByIdAndUser(id, user)
                .orElseThrow(() -> new ResourceNotFoundException("Notification not found with id: " + id));
        notification.setIsRead(true);
        return notificationMapper.toResponse(notification);
    }
}
