package com.example.mate.domain.goodsReview.repository;

import com.example.mate.domain.goodsReview.entity.GoodsReview;
import org.springframework.data.jpa.repository.JpaRepository;

public interface GoodsReviewRepository extends JpaRepository<GoodsReview, Long> {

    int countByRevieweeId(Long revieweeId);

    boolean existsByGoodsPostIdAndReviewerId(Long goodsPostId, Long reviewerId);
}
