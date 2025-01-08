package com.example.mate.domain.matePost.repository;

import com.example.mate.domain.matePost.entity.MateReview;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;

public interface MateReviewRepository extends JpaRepository<MateReview, Long> {

    int countByRevieweeId(Long revieweeId);

    @Query("""
            SELECT mr
            FROM MateReview mr
            WHERE mr.visit.id = :visitId
            AND mr.reviewer.id = :reviewerId
            ORDER BY mr.reviewee.id ASC
            """)
    List<MateReview> findMateReviewsByVisitIdAndReviewerId(@Param("visitId") Long visitId,
                                                           @Param("reviewerId") Long reviewerId);
}
