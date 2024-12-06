package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatMessageResponse {
    private Long messageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private String messageType;
    private String senderImageUrl;
    private LocalDateTime sendTime;

    public static MateChatMessageResponse of(MateChatMessage message) {
        return MateChatMessageResponse.builder()
                .messageId(message.getId())
                .roomId(message.getMateChatRoom().getId())
                .senderId(message.getSender().getId())
                .senderNickname(message.getSender().getNickname())
                .message(message.getContent())
                .messageType(message.getType().getValue())
                .senderImageUrl(message.getSender().getImageUrl())
                .sendTime(message.getSendTime())
                .build();
    }
}
