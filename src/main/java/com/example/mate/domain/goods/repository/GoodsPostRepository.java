package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

public interface GoodsPostRepository extends JpaRepository<GoodsPost, Long> {

    @Query("SELECT COUNT(gp) FROM GoodsPost gp WHERE gp.seller.id = :memberId AND gp.status = :status")
    int countGoodsPostsBySellerIdAndStatus(Long memberId, Status status);
}
