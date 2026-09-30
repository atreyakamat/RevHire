package com.revhire.notificationservice.repository;

import com.revhire.notificationservice.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    /**
     * Find all notifications for a specific user (paginated)
     */
    Page<Notification> findByRecipientIdOrderByCreatedAtDesc(Long recipientId, Pageable pageable);

    /**
     * Count unread notifications for a user
     */
    long countByRecipientIdAndIsReadFalse(Long recipientId);

    /**
     * Mark all unread notifications as read for a user
     */
    @Modifying
    @Query("UPDATE Notification n SET n.isRead = true WHERE n.recipientId = :recipientId AND n.isRead = false")
    int markAllAsReadByRecipientId(@Param("recipientId") Long recipientId);

    /**
     * Check if notification belongs to user (for authorization)
     */
    @Query("SELECT CASE WHEN COUNT(n) > 0 THEN true ELSE false END FROM Notification n WHERE n.id = :notificationId AND n.recipientId = :recipientId")
    boolean notificationBelongsToUser(@Param("notificationId") Long notificationId, @Param("recipientId") Long recipientId);

    /**
     * Find notification by ID and Recipient ID (for security)
     */
    Optional<Notification> findByIdAndRecipientId(Long id, Long recipientId);
}