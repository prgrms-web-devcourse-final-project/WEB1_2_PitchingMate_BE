package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import java.util.Optional;
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
            """)
    Optional<GoodsChatRoom> findExistingChatRoom(@Param("postId") Long postId, @Param("buyerId") Long buyerId,
                                                 @Param("role") Role role);
}
