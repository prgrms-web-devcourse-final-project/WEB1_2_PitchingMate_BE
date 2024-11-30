package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.GoodsReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReviewRepository extends JpaRepository<GoodsReview, Long> {

    int countByRevieweeId(Long revieweeId);
}