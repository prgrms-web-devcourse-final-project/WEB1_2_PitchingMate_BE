package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;

@Repository
public interface MateRepository extends JpaRepository<MatePost, Long>, MateRepositoryCustom {
    @Query("""
             SELECT mp FROM MatePost mp JOIN FETCH mp.match mt
             WHERE (:teamId IS NULL OR mp.teamId = :teamId)
             And mt.matchTime > :now AND mp.status IN :statuses
             ORDER BY mt.matchTime ASC
            """)
    List<MatePost> findMainPagePosts(@Param("teamId") Long teamId, @Param("now") LocalDateTime now,
                                     @Param("statuses") List<Status> statuses, Pageable pageable);

    @Query("""
            SELECT mp
            FROM MatePost mp
            WHERE mp.author.id = :memberId
            ORDER BY mp.createdAt DESC
            """)
    Page<MatePost> findMyMatePosts(@Param("memberId") Long memberId, Pageable pageable);
}
