package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.message.MessageType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatMessageResponse {
    private MessageType type;
    private Long roomId;
    private Long senderId;
    private String senderNickname;
    private String message;
    private LocalDateTime timestamp;
    private Integer currentMembers;

    public static MateChatMessageResponse of(MateChatMessageRequest request, String senderNickname, Integer currentMembers) {
        return MateChatMessageResponse.builder()
                .type(request.getType())
                .roomId(request.getRoomId())
                .senderId(request.getSenderId())
                .senderNickname(senderNickname)
                .message(request.getMessage())
                .timestamp(request.getTimestamp())
                .currentMembers(currentMembers)
                .build();
    }
}
