package com.example.mate.domain.mateChat.dto.request;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class MateChatMessageRequest {
    private String type;
    private Long roomId;
    private Long senderId;
    private String message;

    public static MateChatMessage from(MateChatRoom mateChatRoom, MateChatMessageRequest message, Member sender) {
        return MateChatMessage.builder()
                .mateChatRoom(mateChatRoom)
                .sender(sender)
                .type(MessageType.valueOf(message.getType()))
                .content(message.getMessage())
                .build();
    }
}