package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.time.LocalDateTime;

public interface MateChatMessageRepository extends JpaRepository<MateChatMessage, Long> {

    @Query("SELECT m FROM MateChatMessage m " +
            "WHERE m.mateChatRoom.id = :roomId " +
            "AND m.createdAt >= :enterTime " +
            "ORDER BY m.createdAt ASC")
    Page<MateChatMessage> findByChatRoomIdAndCreatedAtAfterOrderByCreatedAtDesc(@Param("roomId") Long roomId,
                                                                                @Param("enterTime") LocalDateTime enterTime,
                                                                                Pageable pageable);
}