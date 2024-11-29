package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.MateReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface MateReviewRepository extends JpaRepository<MateReview, Long> {

    int countByRevieweeId(Long revieweeId);
}
