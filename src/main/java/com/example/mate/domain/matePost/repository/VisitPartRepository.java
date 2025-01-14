package com.example.mate.domain.matePost.repository;

import com.example.mate.domain.matePost.entity.VisitPart;
import com.example.mate.domain.matePost.entity.VisitPartId;
import com.example.mate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitPartRepository extends JpaRepository<VisitPart, VisitPartId> {

    int countByMember(Member member);

    @Query("""
            SELECT vp.member
            FROM VisitPart vp
            WHERE vp.visit.id = :visitId
            AND vp.member.id != :memberId
            ORDER BY vp.member.id ASC
            """)
    List<Member> findMembersByVisitIdExcludeMember(@Param("visitId") Long visitId, @Param("memberId") Long memberId);

    @Query("""
            SELECT COUNT(vp) > 0
            FROM VisitPart vp
            WHERE vp.visit.id = :visitId
            AND vp.member.id = :memberId
            """)
    boolean existsByVisitAndMember(@Param("visitId") Long visitId, @Param("memberId") Long memberId);

}
