package com.example.mate.domain.goodsChat.event;

import com.example.mate.domain.goodsChat.service.GoodsChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.scheduling.annotation.Async;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Propagation;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.transaction.event.TransactionalEventListener;

@Component
@RequiredArgsConstructor
public class GoodsChatEventHandler {

    private final GoodsChatMessageService messageService;

    @Async
    @TransactionalEventListener
    @Transactional(propagation = Propagation.REQUIRES_NEW)
    public void handle(GoodsChatEvent event) {
        messageService.sendChatEventMessage(event);
    }
}
