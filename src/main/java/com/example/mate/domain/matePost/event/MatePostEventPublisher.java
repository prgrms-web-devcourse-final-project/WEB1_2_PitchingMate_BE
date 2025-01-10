package com.example.mate.domain.matePost.event;

import lombok.RequiredArgsConstructor;
import org.springframework.context.ApplicationEventPublisher;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class MatePostEventPublisher {

    private final ApplicationEventPublisher applicationEventPublisher;

    public void publish(MatePostEvent matePostEvent) {
        applicationEventPublisher.publishEvent(matePostEvent);
    }
}
