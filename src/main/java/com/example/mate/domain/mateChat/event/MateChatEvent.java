package com.example.mate.domain.mateChat.event;

import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;

public record MateChatEvent(Long chatRoomId, Member member, MessageType type) {
    public static MateChatEvent from(Long chatRoomId, Member member, MessageType type) {
        return new MateChatEvent(chatRoomId, member, type);
    }
}