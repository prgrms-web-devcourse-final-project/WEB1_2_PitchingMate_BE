package com.example.mate.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    MATE_CLOSED("모집완료") {
        @Override
        public String generateContent() {
            return "메이트 모집에 참여되었습니다.";
        }
    },
    MATE_COMPLETE("직관완료") {
        @Override
        public String generateContent() {
            return "직관 후기를 남겨주세요!";
        }
    },
    GOODS_CLOSED("거래완료") {
        @Override
        public String generateContent() {
            return "굿즈 거래 후기를 남겨주세요!";
        }
    };

    private final String value;

    public abstract String generateContent();
}
