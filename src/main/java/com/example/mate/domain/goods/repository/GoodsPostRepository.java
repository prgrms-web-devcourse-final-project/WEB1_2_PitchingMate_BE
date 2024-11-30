package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Status;
import java.util.List;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsPostRepository extends JpaRepository<GoodsPost, Long> {

    @Query("""
            SELECT COUNT(gp) FROM GoodsPost gp WHERE
            gp.seller.id = :memberId
            AND gp.status = :status
            """)
    int countGoodsPostsBySellerIdAndStatus(Long memberId, Status status);

    @Query("""
            SELECT gp FROM GoodsPost gp
            WHERE (:teamId IS NULL OR gp.teamId = :teamId)
            AND gp.status = :status
            ORDER BY gp.createdAt DESC
            """)
    List<GoodsPost> findMainGoodsPosts(@Param("teamId") Long teamId, @Param("status") Status status, Pageable pageable);
}
