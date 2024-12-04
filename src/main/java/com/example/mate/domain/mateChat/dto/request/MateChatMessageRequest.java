package com.example.mate.domain.mateChat.dto.request;

import com.example.mate.domain.mateChat.entity.MateChatMessage;
import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;
import lombok.*;

@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor
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

    // 입장 메시지용 메서드
    public static MateChatMessageRequest createEnterMessage(Long roomId, Long senderId, String nickname) {
        return MateChatMessageRequest.builder()
                .type(MessageType.ENTER.name())
                .roomId(roomId)
                .senderId(senderId)
                .message(nickname + "님이 입장하셨습니다.")
                .build();
    }

    public static MateChatMessageRequest createLeaveMessage(Long roomId, Long senderId, String nickname) {
        return MateChatMessageRequest.builder()
                .type(MessageType.LEAVE.name())
                .roomId(roomId)
                .senderId(senderId)
                .message(nickname + "님이 퇴장하셨습니다.")
                .build();
    }
}