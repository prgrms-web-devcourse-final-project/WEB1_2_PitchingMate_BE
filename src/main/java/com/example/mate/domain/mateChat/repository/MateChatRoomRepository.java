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

    // 멤버의 활성화된 채팅방 목록을 조회
    // 자신이 속한 채팅방 중 활성화된 채팅방만 조회
    // 채팅방이 활성화 상태여야 함
    // 멤버가 활성화 상태여야 함
    // 마지막 채팅 시간 기준 내림차순 정렬
    @Query("SELECT DISTINCT cr FROM MateChatRoom cr " +
            "JOIN FETCH cr.matePost mp " +
            "JOIN MateChatRoomMember crm ON crm.mateChatRoom = cr " +
            "WHERE crm.member.id = :memberId " +
            "AND crm.isActive = true " +
            "AND cr.isActive = true " +
            "ORDER BY cr.lastChatSentAt DESC")
    Page<MateChatRoom> findActiveChatRoomsByMemberId(@Param("memberId") Long memberId, Pageable pageable);
}