package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.VisitPart;
import com.example.mate.domain.mate.entity.VisitPartId;
import com.example.mate.domain.member.entity.Member;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface VisitPartRepository extends JpaRepository<VisitPart, VisitPartId> {

    int countByMember(Member member);

    @Query("""
            SELECT vp.member
            FROM VisitPart vp
            WHERE vp.visit.id = :visitId
            AND vp.member.id != :memberId
            """)
    List<Member> findMembersByVisitIdExcludeMember(@Param("visitId") Long visitId, @Param("memberId") Long memberId);
}
