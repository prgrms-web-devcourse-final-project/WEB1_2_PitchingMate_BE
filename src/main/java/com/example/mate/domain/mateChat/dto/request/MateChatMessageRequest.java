package com.example.mate.domain.mateChat.dto.request;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

import java.time.LocalDateTime;

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

    public static MateChatMessage toEntity(MateChatRoom chatRoom, MateChatMessageRequest request, Member sender) {
        return MateChatMessage.builder()
                .mateChatRoom(chatRoom)
                .sender(sender)
                .type(MessageType.valueOf(request.getType()))
                .content(request.getMessage())
                .sendTime(LocalDateTime.now())
                .build();
    }

    public static MateChatMessageRequest createEnterMessage(Long roomId, Long senderId, String nickname) {
        return MateChatMessageRequest.builder()
                .type(MessageType.ENTER.name())
                .roomId(roomId)
                .senderId(senderId)
                .message(nickname + "님이 들어왔습니다.")
                .build();
    }

    public static MateChatMessageRequest createLeaveMessage(Long roomId, Long senderId, String nickname) {
        return MateChatMessageRequest.builder()
                .type(MessageType.LEAVE.name())
                .roomId(roomId)
                .senderId(senderId)
                .message(nickname + "님이 나갔습니다.")
                .build();
    }
}