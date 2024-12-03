package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.QMatePost;
import com.example.mate.domain.mate.entity.QMateReview;
import com.example.mate.domain.member.dto.response.MyReviewResponse;
import com.example.mate.domain.member.entity.QMember;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class MateReviewRepositoryCustomImpl implements MateReviewRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    public Page<MyReviewResponse> findMateReviewsByRevieweeId(Long revieweeId, Pageable pageable) {
        QMateReview mateReview = QMateReview.mateReview;
        QMatePost matePost = QMatePost.matePost;
        QMember reviewer = QMember.member;

        // 'created_at' 기준 내림차순으로 고정된 정렬
        OrderSpecifier<LocalDateTime> desc = mateReview.createdAt.desc();

        // 페이징 처리된 데이터 조회
        List<MyReviewResponse> results = queryFactory
                .select(Projections.constructor(
                        MyReviewResponse.class,
                        matePost.id.as("postId"),
                        matePost.title.as("title"),
                        reviewer.nickname.as("nickname"),
                        mateReview.rating.stringValue().as("rating"),
                        mateReview.reviewContent.as("content"),
                        mateReview.createdAt.as("createdAt")
                ))
                .from(mateReview)
                .join(mateReview.visit.post, matePost)
                .join(mateReview.reviewer, reviewer)
                .where(mateReview.reviewee.id.eq(revieweeId))
                .orderBy(desc)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 데이터 개수 조회 (null 체크 추가)
        Long total = queryFactory
                .select(mateReview.count())
                .from(mateReview)
                .where(mateReview.reviewee.id.eq(revieweeId))
                .fetchOne();

        // total이 null일 경우 0으로 처리
        long totalElements = total != null ? total : 0;

        // Page 객체 생성
        return new PageImpl<>(results, pageable, totalElements);
    }
}
