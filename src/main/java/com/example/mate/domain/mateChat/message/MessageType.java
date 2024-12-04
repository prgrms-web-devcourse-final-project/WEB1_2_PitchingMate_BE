package com.example.mate.domain.mateChat.message;

import lombok.Getter;

@Getter
public enum MessageType {
    ENTER("입장"),      // 채팅방 입장
    TALK("대화"),       // 일반 메시지
    LEAVE("퇴장");      // 채팅방 퇴장

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

}
