package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import java.util.Optional;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsChatRoomRepository extends JpaRepository<GoodsChatRoom, Long> {


    @Query("""
            SELECT cr
            FROM GoodsChatRoom cr
            JOIN cr.chatParts cp
            WHERE cr.goodsPost.id = :postId
            AND cp.member.id = :buyerId
            AND cp.role = :role
            AND cp.isActive = true
            """)
    Optional<GoodsChatRoom> findExistingChatRoom(@Param("postId") Long postId, @Param("buyerId") Long buyerId,
                                                 @Param("role") Role role);

    @Query("""
            SELECT cr
            FROM GoodsChatRoom cr
            JOIN FETCH cr.chatParts cp
            JOIN FETCH cr.goodsPost gp
            JOIN FETCH cp.member m
            WHERE cr.id IN (
                SELECT gcr.id
                FROM GoodsChatRoom gcr
                JOIN gcr.chatParts gcp
                WHERE gcp.member.id = :memberId
                AND gcp.isActive = true
            )
            ORDER BY cr.lastChatSentAt DESC
            """)
    Page<GoodsChatRoom> findChatRoomPageByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT cr
            FROM GoodsChatRoom cr
            JOIN FETCH cr.goodsPost gp
            WHERE cr.id = :chatRoomId
            """)
    Optional<GoodsChatRoom> findByChatRoomId(@Param("chatRoomId") Long chatRoomId);
}
