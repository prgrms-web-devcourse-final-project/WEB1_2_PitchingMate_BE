package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.document.MateChatMessage;
import com.example.mate.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatMessageResponse {
    private String messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String senderImageUrl;
    private String message;
    private String messageType;
    private LocalDateTime sendTime;

    public static MateChatMessageResponse from(MateChatMessage message, Member sender) {
        return MateChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getRoomId())
                .senderId(message.getSenderId())
                .senderNickname(sender.getNickname())
                .senderImageUrl(sender.getImageUrl())
                .message(message.getContent())
                .messageType(message.getType().getValue())
                .sendTime(message.getSendTime())
                .build();
    }
}
