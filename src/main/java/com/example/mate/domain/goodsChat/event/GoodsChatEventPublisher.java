package com.example.mate.domain.goodsChat.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class GoodsChatEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(GoodsChatEvent goodsChatEvent) {
        applicationEventPublisher.publishEvent(goodsChatEvent);
    }
}
