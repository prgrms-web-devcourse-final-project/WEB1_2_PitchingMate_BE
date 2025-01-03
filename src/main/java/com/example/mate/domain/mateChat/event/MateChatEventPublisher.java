package com.example.mate.domain.mateChat.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MateChatEventPublisher {
    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(MateChatEvent mateChatEvent) {
        applicationEventPublisher.publishEvent(mateChatEvent);
    }
}
