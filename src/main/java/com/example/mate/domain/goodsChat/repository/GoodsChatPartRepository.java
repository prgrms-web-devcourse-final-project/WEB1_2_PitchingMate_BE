package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.goodsChat.entity.GoodsChatPartId;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsChatPartRepository extends JpaRepository<GoodsChatPart, GoodsChatPartId> {

    @Query("""
            SELECT cr
            FROM GoodsChatPart cr
            JOIN FETCH cr.member crm
            WHERE cr.goodsChatRoom.id = :chatRoomId
            AND cr.isActive = true
            """)
    List<GoodsChatPart> findAllWithMemberByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}