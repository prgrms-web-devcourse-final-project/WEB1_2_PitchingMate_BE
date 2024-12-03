package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsChatMessageRepository extends JpaRepository<GoodsChatMessage, Long> {

    @Query("""
            SELECT cm
            FROM GoodsChatMessage cm
            WHERE cm.goodsChatPart.goodsChatRoom.id = :chatRoomId
            ORDER BY cm.sentAt DESC
            """)
    Page<GoodsChatMessage> findByChatRoomId(@Param("chatRoomId") Long chatRoomId, Pageable pageable);
}
