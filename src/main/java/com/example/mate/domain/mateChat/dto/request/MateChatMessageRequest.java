package com.example.mate.domain.mateChat.dto.request;

import com.example.mate.domain.mateChat.message.MessageType;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateChatMessageRequest {
    private MessageType type;
    private Long roomId;
    private Long senderId;
    private String message;
    private LocalDateTime timestamp;

    @Builder
    private MateChatMessageRequest(MessageType type, Long roomId, Long senderId, String message) {
        this.type = type;
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
        this.timestamp = LocalDateTime.now();
    }
}