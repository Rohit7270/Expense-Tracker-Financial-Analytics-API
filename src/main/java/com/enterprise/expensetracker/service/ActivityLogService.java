package com.enterprise.expensetracker.service;

import com.enterprise.expensetracker.entity.ActivityLog;
import com.enterprise.expensetracker.entity.User;
import com.enterprise.expensetracker.repository.ActivityLogRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

@Service
public class ActivityLogService {

    @Autowired
    private ActivityLogRepository activityLogRepository;

    @Transactional
    public void log(String action, String details, User user) {
        ActivityLog log = ActivityLog.builder()
                .action(action)
                .details(details)
                .timestamp(LocalDateTime.now())
                .user(user)
                .build();
        activityLogRepository.save(log);
    }

    public List<ActivityLog> getLogsForUser(Long userId) {
        return activityLogRepository.findByUserIdOrderByTimestampDesc(userId);
    }
}
