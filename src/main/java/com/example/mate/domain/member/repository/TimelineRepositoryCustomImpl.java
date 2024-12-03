package com.example.mate.domain.member.repository;

import static com.example.mate.domain.mate.entity.QVisit.visit;
import static com.example.mate.domain.mate.entity.QVisitPart.visitPart;
import static com.example.mate.domain.member.entity.QMember.member;

import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import com.querydsl.core.types.Projections;
import com.querydsl.jpa.impl.JPAQuery;
import com.querydsl.jpa.impl.JPAQueryFactory;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.support.PageableExecutionUtils;
import org.springframework.stereotype.Repository;

@Repository
@RequiredArgsConstructor
public class TimelineRepositoryCustomImpl implements TimelineRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<MyTimelineResponse> findVisitsById(Long memberId, Pageable pageable) {
        List<MyTimelineResponse> results = queryFactory
                .select(Projections.constructor(
                        MyTimelineResponse.class,
                        visit.id.as("visitId"),
                        visit.post.id.as("matePostId"),
                        visitPart.member.id.as("memberId")
                ))
                .from(visit)
                .join(visit.participants, visitPart) // Visit -> VisitPart 조인
                .join(visitPart.member, member) // VisitPart -> Member 조인
                .where(member.id.eq(memberId))
                .orderBy(visit.id.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(visit.count())
                .from(visit)
                .join(visit.participants, visitPart)
                .join(visitPart.member, member)
                .where(member.id.eq(memberId));

        return PageableExecutionUtils.getPage(results, pageable, total::fetchOne);
    }
}
