package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MateChatRoomRepository extends JpaRepository<MateChatRoom, Long> {
    Optional<MateChatRoom> findByMatePostId(Long matePostId);

    @Query("SELECT cr FROM MateChatRoom cr " +
            "JOIN FETCH cr.matePost mp " +
            "JOIN cr.members crm " +
            "WHERE crm.member.id = :memberId " +
            "AND crm.isActive = true " +
            "ORDER BY cr.lastChatSentAt DESC")
    Page<MateChatRoom> findActiveChatRoomsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    boolean existsByMatePostId(Long matePostId);
}