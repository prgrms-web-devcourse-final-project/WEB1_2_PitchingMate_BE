package com.example.mate.domain.notification.repository;

import com.example.mate.domain.notification.entity.Notification;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

@Repository
public interface NotificationRepository extends JpaRepository<Notification, Long> {

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.receiver.id = :receiverId
            AND (n.notificationType = 'MATE_CLOSED' OR n.notificationType = 'MATE_COMPLETE')
            ORDER BY n.id DESC
            """)
    Page<Notification> findMateNotificationsByReceiverId(@Param("receiverId") Long receiverId,
                                                         Pageable pageable);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.receiver.id = :receiverId
            AND n.notificationType = 'GOODS_CLOSED'
            ORDER BY n.id DESC
            """)
    Page<Notification> findGoodsNotificationsByReceiverId(@Param("receiverId") Long receiverId,
                                                          Pageable pageable);

    @Query("""
            SELECT n
            FROM Notification n
            WHERE n.receiver.id = :receiverId
            ORDER BY n.id DESC
            """)
    Page<Notification> findNotificationsByReceiverId(@Param("receiverId") Long receiverId, Pageable pageable);
}
