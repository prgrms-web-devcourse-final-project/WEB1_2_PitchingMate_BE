package com.example.mate.domain.goodsChat.dto.request;

import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@ToString
public class GoodsChatMessageRequest {

    private Long roomId;
    private Long senderId;
    private String message;
    private MessageType type;

    @Builder
    private GoodsChatMessageRequest(MessageType type, Long roomId, Long senderId, String message) {
        this.type = type;
        this.roomId = roomId;
        this.senderId = senderId;
        this.message = message;
    }

    public static GoodsChatMessage from(GoodsChatMessageRequest message, GoodsChatPart chatPart, MessageType type) {
        return GoodsChatMessage.builder()
                .goodsChatPart(chatPart)
                .content(message.getMessage())
                .messageType(type)
                .build();
    }
}
