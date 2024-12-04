package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.Optional;

public interface MateChatMessageRepository extends JpaRepository<MateChatMessage, Long> {

    @Query("SELECT cm FROM MateChatMessage cm " +
            "WHERE cm.mateChatRoom.id = :roomId " +
            "ORDER BY cm.createdAt DESC")
    Page<MateChatMessage> findByChatRoomIdOrderByCreatedAtDesc(@Param("roomId") Long roomId, Pageable pageable);

    @Query("SELECT cm FROM MateChatMessage cm " +
            "WHERE cm.mateChatRoom.id = :roomId " +
            "ORDER BY cm.createdAt DESC " +
            "LIMIT 1")
    Optional<MateChatMessage> findLatestMessageByChatRoomId(@Param("roomId") Long roomId);

}