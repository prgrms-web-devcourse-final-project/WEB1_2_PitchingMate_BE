package com.example.mate.domain.constant;

import lombok.Getter;

@Getter
public enum MessageType {
    ENTER("입장"),      // 채팅방 입장
    TALK("대화"),       // 일반 메시지
    LEAVE("퇴장"),      // 채팅방 퇴장
    GOODS("굿즈");      // 굿즈 거래완료

    private final String value;

    MessageType(String value) {
        this.value = value;
    }

}
