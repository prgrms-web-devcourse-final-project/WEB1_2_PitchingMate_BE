package com.example.mate.domain.goodsPost.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoodsPostEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(GoodsPostEvent goodsPostEvent) {
        applicationEventPublisher.publishEvent(goodsPostEvent);
    }
}
