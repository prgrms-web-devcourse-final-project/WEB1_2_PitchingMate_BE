package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.document.MateChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

import java.time.LocalDateTime;

public interface MateChatMessageRepository extends MongoRepository<MateChatMessage, String> {
    // 채팅방의 특정 시간 이후 메시지 조회 (페이징)
    @Query("{ 'roomId': ?0, 'sendTime': { $gt: ?1 } }")
    Page<MateChatMessage> findByRoomIdAndSendTimeAfter(
            Long roomId,
            LocalDateTime enterTime,
            Pageable pageable
    );
}