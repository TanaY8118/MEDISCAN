package com.mediscan.notification.repository;

import com.mediscan.notification.entity.NotificationDeviceToken;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface NotificationDeviceTokenRepository extends JpaRepository<NotificationDeviceToken, Long> {
    Optional<NotificationDeviceToken> findByUserIdAndToken(Long userId, String token);

    List<NotificationDeviceToken> findByUserId(Long userId);

    List<NotificationDeviceToken> findByUserIdIn(List<Long> userIds);
}
