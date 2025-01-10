package com.example.mate.domain.matePost.event;

import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MatePostEventHandler {

    private final NotificationService notificationService;

    private static final String NOTIFICATION_MESSAGE_CLOSED = "메이트 모집에 참여되었습니다.";
    private static final String NOTIFICATION_MESSAGE_COMPLETE = "직관 후기를 남겨주세요!";
    private static final String BASE_URL = "http://localhost:5173/api/mates/";    // TODO : 프론트엔드 배포 후 변경 필요

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(MatePostEvent event) {
        NotificationType type = event.notificationType();
        notificationService.send(type, getMessage(event.matePostTitle(), type),
                getUrl(event.matePostId()), event.receiver());
    }

    private String getMessage(String matePostTitle, NotificationType type) {
        String message =
                type == NotificationType.MATE_CLOSED ? NOTIFICATION_MESSAGE_CLOSED : NOTIFICATION_MESSAGE_COMPLETE;
        return "<" + matePostTitle + "> " + message;
    }

    private String getUrl(Long matePostId) {
        return BASE_URL + matePostId;
    }
}
