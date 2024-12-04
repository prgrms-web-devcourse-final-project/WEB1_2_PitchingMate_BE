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
    @Query("SELECT crm FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.member.id = :memberId")
    Optional<MateChatRoomMember> findByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId,
                                                             @Param("memberId") Long memberId
    );

    @Query("SELECT COUNT(crm) > 0 FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.member.id = :memberId")
    boolean existsByChatRoomIdAndMemberId(@Param("chatRoomId") Long chatRoomId,
                                          @Param("memberId") Long memberId
    );

    @Query("SELECT crm FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.isActive = true")
    List<MateChatRoomMember> findAllByChatRoomIdAndIsActiveTrue(@Param("chatRoomId") Long chatRoomId
    );

    @Query("SELECT COUNT(crm) FROM MateChatRoomMember crm " +
            "WHERE crm.mateChatRoom.id = :chatRoomId " +
            "AND crm.isActive = true")
    int countByChatRoomIdAndIsActiveTrue(@Param("chatRoomId") Long chatRoomId
    );
}
