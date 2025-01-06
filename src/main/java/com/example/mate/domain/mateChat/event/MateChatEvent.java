package com.example.mate.domain.mateChat.event;

import com.example.mate.domain.mateChat.dto.request.MateChatMessageRequest;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;

public record MateChatEvent(Long chatRoomId, Member member, MessageType type) {
    public static MateChatEvent from(Long chatRoomId, Member member, MessageType type) {
        return new MateChatEvent(chatRoomId, member, type);
    }

    public MateChatMessageRequest toMessageRequest() {
        String content = type == MessageType.ENTER ?
                member.getNickname() + "님이 들어왔습니다." :
                member.getNickname() + "님이 나갔습니다.";

        return MateChatMessageRequest.builder()
                .roomId(chatRoomId)
                .senderId(member.getId())
                .type(type.name())
                .message(content)
                .build();
    }
}