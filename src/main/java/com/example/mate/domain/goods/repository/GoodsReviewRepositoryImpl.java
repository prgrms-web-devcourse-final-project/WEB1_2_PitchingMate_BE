package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.QGoodsPost;
import com.example.mate.domain.goods.entity.QGoodsReview;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.entity.QMember;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

@RequiredArgsConstructor
public class GoodsReviewRepositoryImpl implements GoodsReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyReviewResponse> findGoodsReviewsByRevieweeId(Long revieweeId, Pageable pageable) {
        QGoodsReview goodsReview = QGoodsReview.goodsReview;
        QGoodsPost goodsPost = QGoodsPost.goodsPost;
        QMember member = QMember.member;

        // 'created_at' 기준 내림차순으로 고정된 정렬
        OrderSpecifier<LocalDateTime> desc = goodsReview.createdAt.desc();

        // 페이징 처리된 데이터 조회
        List<MyReviewResponse> results = queryFactory
                .select(Projections.constructor(
                        MyReviewResponse.class,
                        goodsPost.id.as("postId"),
                        goodsPost.title.as("title"),
                        member.nickname.as("nickname"),
                        goodsReview.rating.stringValue().as("rating"),
                        goodsReview.reviewContent.as("content"),
                        goodsReview.createdAt.as("createdAt")
                ))
                .from(goodsReview)
                .join(goodsReview.goodsPost, goodsPost)
                .join(goodsReview.reviewer, member)
                .where(goodsReview.reviewee.id.eq(revieweeId))
                .orderBy(desc)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(goodsReview.count())
                .from(goodsReview)
                .where(goodsReview.reviewee.id.eq(revieweeId));

        return PageableExecutionUtils.getPage(results, pageable, total::fetchOne);
    }
}
