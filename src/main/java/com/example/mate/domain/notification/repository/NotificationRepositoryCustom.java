package com.example.mate.domain.notification.repository;

import com.example.mate.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface NotificationRepositoryCustom {

    Page<Notification> findNotificationsPage(String type, Long memberId, Pageable pageable);
}
