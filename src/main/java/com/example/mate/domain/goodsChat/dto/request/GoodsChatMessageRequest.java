package com.example.mate.domain.goodsChat.dto.request;

import com.example.mate.domain.constant.MessageType;
import lombok.Getter;

@Getter
public class GoodsChatMessageRequest {

    private Long roomId;
    private Long senderId;
    private String message;
    private MessageType type;
}
