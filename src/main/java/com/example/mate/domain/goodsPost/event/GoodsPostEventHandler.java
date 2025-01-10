package com.example.mate.domain.goodsPost.event;

import com.example.mate.domain.notification.service.NotificationService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GoodsPostEventHandler {

    private final NotificationService notificationService;

    private static final String NOTIFICATION_MESSAGE = "굿즈 거래 후기를 남겨주세요!";
    private static final String BASE_URL = "http://localhost:5173/api/goods/";    // TODO : 프론트엔드 배포 후 변경 필요

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(GoodsPostEvent event) {
        notificationService.send(event.notificationType(), NOTIFICATION_MESSAGE, getUrl(event.goodsPostId()),
                event.receiver());
    }

    private String getUrl(Long goodsPostId) {
        return BASE_URL + goodsPostId;
    }
}
