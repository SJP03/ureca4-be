package com.ureca.billing.notification.domain.repository;

import com.ureca.billing.notification.domain.entity.Notification;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationRepository extends CrudRepository<Notification, Long> {
    
    Optional<Notification> findByBillIdAndNotificationType(Long billId, String notificationType);
    
    List<Notification> findByNotificationStatus(String status);
}