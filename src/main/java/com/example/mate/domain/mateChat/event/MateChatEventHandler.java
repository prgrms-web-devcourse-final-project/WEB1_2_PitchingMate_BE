package com.example.mate.domain.mateChat.event;

import com.example.mate.domain.mateChat.service.MateChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class MateChatEventHandler {
    private final MateChatMessageService messageService;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(MateChatEvent event) {
        messageService.sendChatEventMessage(event);
    }
}
