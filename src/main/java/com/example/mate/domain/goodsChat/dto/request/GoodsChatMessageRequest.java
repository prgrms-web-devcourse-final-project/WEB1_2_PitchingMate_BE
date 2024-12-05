package com.example.mate.domain.goodsChat.dto.request;

import com.example.mate.domain.constant.MessageType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.Getter;

@Getter
public class GoodsChatMessageRequest {

    @NotNull(message = "채팅방 ID는 필수 입력 값입니다.")
    private Long roomId;

    @NotNull(message = "회원 ID는 필수 입력 값입니다.")
    private Long senderId;

    @NotBlank(message = "메시지는 비어있을 수 없습니다.")
    private String message;

    @NotNull(message = "채팅 타입은 필수 입력 값입니다.")
    private MessageType type;
}
