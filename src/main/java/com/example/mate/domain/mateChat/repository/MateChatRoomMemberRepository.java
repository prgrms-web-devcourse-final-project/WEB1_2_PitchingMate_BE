package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface MateChatRoomMemberRepository extends JpaRepository<MateChatRoomMember, Long> {

    // 특정 채팅방의 특정 멤버 조회
    @Query("SELECT crm FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.member.id = :memberId")
    Optional<MateChatRoomMember> findByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId,
                                                             @Param("memberId") Long memberId
    );

    // 특정 채팅방의 활성화된 멤버 수 카운트
    @Query("SELECT COUNT(crm) FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.isActive = true")
    int countByChatRoomIdAndIsActiveTrue(@Param("chatRoomId") Long chatRoomId);

    // 특정 채팅방의 활성화된 멤버 목록 조회
    @Query("SELECT crm FROM MateChatRoomMember crm " +
            "JOIN FETCH crm.member " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.isActive = true")
    List<MateChatRoomMember> findActiveMembers(@Param("chatRoomId") Long chatRoomId);
}
