package com.example.mate.domain.mate.repository;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.match.entity.QMatch;
import com.example.mate.domain.mate.dto.request.MatePostSearchRequest;
import com.example.mate.domain.mate.entity.*;
import com.example.mate.domain.member.entity.QMember;
import com.querydsl.core.BooleanBuilder;
import com.querydsl.core.types.Order;
import com.querydsl.core.types.OrderSpecifier;
import com.querydsl.jpa.impl.JPAQueryFactory;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.Pageable;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MateRepositoryImpl implements MateRepositoryCustom{
    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MatePost> findMatePostsByFilter(MatePostSearchRequest request, Pageable pageable) {
        QMatePost matePost = QMatePost.matePost;
        QMatch match = QMatch.match;
        QMember author = QMember.member;

        // 기본 조건 설정 (미래 경기, 모집 상태)
        BooleanBuilder builder = new BooleanBuilder();
        builder.and(match.matchTime.after(LocalDateTime.now()));
        builder.and(matePost.status.in(Status.OPEN, Status.CLOSED));

        // 동적 필터 조건 추가
        if (request.getTeamId() != null) {
            builder.and(matePost.teamId.eq(request.getTeamId()));
        }

        if (request.getAge() != null && request.getAge() != Age.ALL) {
            builder.and(matePost.age.eq(request.getAge()));
        }

        if (request.getGender() != null && request.getGender() != Gender.ANY) {
            builder.and(matePost.gender.eq(request.getGender()));
        }

        if (request.getMaxParticipants() != null) {
                builder.and(matePost.maxParticipants.eq(request.getMaxParticipants()));
        }

        if (request.getTransportType() != null && request.getTransportType() != TransportType.ANY) {
            builder.and(matePost.transport.eq(request.getTransportType()));
        }

        // 정렬 조건 설정
        OrderSpecifier<?> orderSpecifier = createOrderSpecifier(request.getSortType(), matePost, match, author);

        // 쿼리 실행
        List<MatePost> content = queryFactory
                .selectFrom(matePost)
                .join(matePost.match, match).fetchJoin()
                .join(matePost.author, author).fetchJoin()
                .where(builder)
                .orderBy(orderSpecifier)
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        // 전체 개수 조회
        long total = queryFactory
                .selectFrom(matePost)
                .join(matePost.match, match)
                .join(matePost.author, author)
                .where(builder)
                .fetch()
                .size();

        return new PageImpl<>(content, pageable, total);
    }

    private OrderSpecifier<?> createOrderSpecifier(SortType sortType,
                                                   QMatePost matePost,
                                                   QMatch match,
                                                   QMember author) {
        if (sortType == null) {
            return new OrderSpecifier<>(Order.DESC, matePost.id); // 기본 정렬
        }

        return switch (sortType) {
            case LATEST -> new OrderSpecifier<>(Order.DESC, matePost.id);
            case MATCH_TIME -> new OrderSpecifier<>(Order.ASC, match.matchTime);
            case MANNER -> new OrderSpecifier<>(Order.DESC, author.manner);
            default -> new OrderSpecifier<>(Order.DESC, matePost.id);
        };
    }
}
