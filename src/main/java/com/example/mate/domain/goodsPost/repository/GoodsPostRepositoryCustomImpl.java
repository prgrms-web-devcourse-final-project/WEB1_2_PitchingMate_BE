package com.example.mate.domain.goodsPost.repository;

import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Status;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;

import java.util.List;

import static com.example.mate.domain.goodsPost.entity.QGoodsPost.goodsPost;

@RequiredArgsConstructor
public class GoodsPostRepositoryCustomImpl implements GoodsPostRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<GoodsPost> findPageGoodsPosts(Long teamId, Status status, Category category, Pageable pageable) {
        BooleanBuilder conditions = buildConditions(teamId, status, category);

        List<GoodsPost> fetch = queryFactory
                .selectFrom(goodsPost)
                .where(conditions.and(goodsPost.seller.isDeleted.isFalse()))
                .orderBy(goodsPost.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(goodsPost.count())
                .from(goodsPost)
                .where(conditions);

        return PageableExecutionUtils.getPage(fetch, pageable, total::fetchOne);
    }

    private BooleanBuilder buildConditions(Long teamId, Status status, Category category) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(goodsPost.status.eq(status));

        if (teamId != null) {
            builder.and(goodsPost.teamId.eq(teamId));
        }
        if (category != null) {
            builder.and(goodsPost.category.eq(category));
        }
        return builder;
    }
}
