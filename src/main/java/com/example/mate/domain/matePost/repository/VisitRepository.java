package com.example.mate.domain.matePost.repository;

import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.matePost.entity.Visit;
import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface VisitRepository extends JpaRepository<Visit, Long> {

    @Query("""
            SELECT new com.example.mate.domain.member.dto.response.MyTimelineResponse(v.id, v.post.id, vp.member.id)
            FROM Visit v
            JOIN v.participants vp
            WHERE vp.member.id = :memberId
            ORDER BY v.id DESC
            """)
    Page<MyTimelineResponse> findVisitsByMemberId(@Param("memberId") Long memberId, Pageable pageable);

    @Query("""
            SELECT m
            FROM Visit v
            JOIN v.post mp
            JOIN mp.match m
            JOIN v.participants vp
            WHERE vp.member.id = :memberId
            ORDER BY v.id DESC
            """)
    List<Match> findMatchesByMemberId(@Param("memberId") Long memberId);
}
