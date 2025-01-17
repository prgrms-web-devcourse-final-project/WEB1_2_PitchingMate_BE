package com.example.mate.domain.notification.repository;

import static com.example.mate.domain.notification.entity.QNotification.notification;

import com.example.mate.domain.notification.dto.response.NotificationResponse;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import com.querydsl.core.BooleanBuilder;
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
public class NotificationRepositoryCustomImpl implements NotificationRepositoryCustom {

    private final JPAQueryFactory queryFactory;

    @Override
    public Page<Notification> findNotificationsPage(String type, Long memberId, Pageable pageable) {
        BooleanBuilder conditions = buildConditions(type, memberId);

        List<Notification> content = queryFactory
                .selectFrom(notification)
                .where(conditions)
                .orderBy(notification.createdAt.desc())
                .offset(pageable.getOffset())
                .limit(pageable.getPageSize())
                .fetch();

        JPAQuery<Long> total = queryFactory
                .select(notification.count())
                .from(notification)
                .where(conditions);

        return PageableExecutionUtils.getPage(content, pageable, total::fetchOne);
    }

    private BooleanBuilder buildConditions(String type, Long memberId) {
        BooleanBuilder builder = new BooleanBuilder();

        builder.and(notification.receiver.id.eq(memberId));

        if (type.equals("mate")) {
            builder.and(notification.notificationType.in(NotificationType.MATE_COMPLETE, NotificationType.MATE_CLOSED));
        } else if (type.equals("goods")) {
            builder.and(notification.notificationType.eq(NotificationType.GOODS_CLOSED));
        }
        return builder;
    }
}
