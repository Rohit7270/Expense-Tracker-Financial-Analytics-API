package com.expensetracker.mapper;

import com.expensetracker.dto.response.NotificationResponse;
import com.expensetracker.entity.Notification;
import org.springframework.stereotype.Component;

/**
 * Mapper for Notification Entity -> Response DTO conversion.
 */
@Component
public class NotificationMapper {

    public NotificationResponse toResponse(Notification notification) {
        if (notification == null) {
            return null;
        }
        return NotificationResponse.builder()
                .id(notification.getId())
                .message(notification.getMessage())
                .type(notification.getType())
                .isRead(notification.getIsRead())
                .createdAt(notification.getCreatedAt())
                .build();
    }
}
