package com.example.mate.domain.mateChat.controller;

import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.service.MateChatMessageService;
import lombok.RequiredArgsConstructor;
import org.springframework.messaging.handler.annotation.MessageMapping;
import org.springframework.messaging.handler.annotation.Payload;
import org.springframework.stereotype.Controller;

@Controller
@RequiredArgsConstructor
public class MateChatMessageController {
    private final MateChatMessageService mateChatMessageService;

    @MessageMapping("/chat/mate/message")
    public void handleMessage(@Payload MateChatMessageRequest message) {
        mateChatMessageService.sendMessage(message);
    }

    @MessageMapping("/chat/mate/leave")
    public void handleLeave(@Payload MateChatMessageRequest message) {
        mateChatMessageService.sendLeaveMessage(message);
    }
}