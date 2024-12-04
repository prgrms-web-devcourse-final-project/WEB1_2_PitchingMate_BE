package com.example.mate.domain.goodsChat.controller;

import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.service.GoodsChatMessageService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
@Slf4j
public class GoodsChatMessageController {

    private final GoodsChatMessageService goodsChatMessageService;

    @MessageMapping("/chat/goods/message")
    public void handleMessage(@Payload GoodsChatMessageRequest message) {
        log.info("message => {}", message);
        goodsChatMessageService.sendMessage(message);
    }

    @MessageMapping("/chat/goods/enter")
    public void handleEnter(@Payload GoodsChatMessageRequest message) {
//        goodsChatMessageService.sendEnterMessage(message);
    }

    @MessageMapping("/chat/goods/leave")
    public void handleLeave(@Payload GoodsChatMessageRequest message) {
//        goodsChatMessageService.sendLeaveMessage(message);
    }
}
