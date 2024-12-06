package com.example.mate.domain.goodsChat.controller;

import com.example.mate.domain.goodsChat.dto.request.GoodsChatMessageRequest;
import com.example.mate.domain.goodsChat.service.GoodsChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;
import org.springframework.validation.annotation.Validated;

@Controller
@RequiredArgsConstructor
public class GoodsChatMessageController {

    private final GoodsChatMessageService goodsChatMessageService;

    @MessageMapping("/chat/goods/message")
    public void handleMessage(@Validated @Payload GoodsChatMessageRequest message) {
        goodsChatMessageService.sendMessage(message);
    }
}
