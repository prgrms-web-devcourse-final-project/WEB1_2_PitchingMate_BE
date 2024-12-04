package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.member.entity.Member;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatMessageResponse {
    private Long chatMessageId;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private String messageType;
    private String senderImageUrl;
    private LocalDateTime sendAt;

    public static MateChatMessageResponse of(MateChatMessage chatMessage) {
        Member sender = chatMessage.getSender();

        return MateChatMessageResponse.builder()
                .chatMessageId(chatMessage.getId())
                .roomId(chatMessage.getMateChatRoom().getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .message(chatMessage.getContent())
                .messageType(chatMessage.getType().getValue())
                .senderImageUrl(sender.getImageUrl())
                .sendAt(chatMessage.getCreatedAt())
                .build();
    }
}
