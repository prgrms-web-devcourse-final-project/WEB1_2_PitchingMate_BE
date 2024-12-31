package com.example.mate.domain.goodsPost.repository;

import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Status;
import java.util.List;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsPostRepository extends JpaRepository<GoodsPost, Long>, GoodsPostRepositoryCustom {

    @Query("""
            SELECT gp
            FROM GoodsPost gp
            WHERE (:teamId IS NULL OR gp.teamId = :teamId)
            AND gp.status = :status
            ORDER BY gp.createdAt DESC
            """)
    List<GoodsPost> findMainGoodsPosts(@Param("teamId") Long teamId, @Param("status") Status status, Pageable pageable);

    @Query("""
            SELECT COUNT(gp)
            FROM GoodsPost gp
            WHERE gp.seller.id = :memberId
            AND gp.status = :status
            """)
    int countGoodsPostsBySellerIdAndStatus(@Param("memberId") Long memberId, @Param("status") Status status);

    @Query("""
            SELECT COUNT(gp)
            FROM GoodsPost gp
            WHERE gp.buyer.id = :memberId
            AND gp.status = :status
            """)
    int countGoodsPostsByBuyerIdAndStatus(@Param("memberId") Long memberId, @Param("status") Status status);

    @EntityGraph(attributePaths = {"goodsPostImages"})
    @Query("""
            SELECT gp
            FROM GoodsPost gp
            WHERE gp.seller.id = :memberId
            AND gp.status = :status
            ORDER BY gp.createdAt DESC
            """)
    Page<GoodsPost> findGoodsPostsBySellerId(@Param("memberId") Long memberId, @Param("status") Status status,
                                             Pageable pageable);

    @EntityGraph(attributePaths = {"goodsPostImages"})
    @Query("""
            SELECT gp
            FROM GoodsPost gp
            WHERE gp.buyer.id = :memberId
            AND gp.status = :status
            ORDER BY gp.createdAt DESC
            """)
    Page<GoodsPost> findGoodsPostsByBuyerId(@Param("memberId") Long memberId, @Param("status") Status status,
                                            Pageable pageable);
}