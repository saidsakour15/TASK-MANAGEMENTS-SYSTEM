package com.stms.service;

import com.stms.dto.response.ApiResponse;
import com.stms.dto.response.NotificationResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationService {
    void createNotification(Long userId, String title, String message);
    Page<NotificationResponse> getNotifications(Long userId, Pageable pageable);
    ApiResponse<?> markAsRead(Long notificationId, Long userId);
    ApiResponse<?> markAllAsRead(Long userId);
    long countUnread(Long userId);
}
