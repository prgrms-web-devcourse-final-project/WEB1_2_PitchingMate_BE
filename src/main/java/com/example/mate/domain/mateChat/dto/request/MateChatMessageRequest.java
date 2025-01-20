package com.example.mate.domain.mateChat.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
public class MateChatMessageRequest {
    @NotNull(message = "메시지 타입은 필수입니다.")
    private String type;

    @NotNull(message = "채팅방 ID는 필수입니다.")
    private Long roomId;

    @NotNull(message = "발신자 ID는 필수입니다.")
    private Long senderId;

    @NotBlank(message = "메시지 내용은 필수입니다.")
    private String message;
}