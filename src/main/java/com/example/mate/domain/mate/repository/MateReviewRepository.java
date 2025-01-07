package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.MateReview;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

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

    boolean existsByVisitIdAndReviewerIdAndRevieweeId(@Param("visitId") Long visitId,
                                                      @Param("reviewerId") Long reviewerId,
                                                      @Param("revieweeId") Long revieweeId);
}
