package com.example.mate.domain.goodsChat.event;

import com.example.mate.domain.constant.MessageType;
import com.example.mate.domain.member.entity.Member;

public record GoodsChatEvent(Long chatRoomId, Member member, MessageType type) {

    public static GoodsChatEvent from(Long chatRoomId, Member member, MessageType type) {
        return new GoodsChatEvent(chatRoomId, member, type);
    }
}
