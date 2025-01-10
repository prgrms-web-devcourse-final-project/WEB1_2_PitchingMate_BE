package com.example.mate.domain.notification.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum NotificationType {

    MATE_CLOSED("모집완료"),
    MATE_COMPLETE("직관완료"),
    GOODS_CLOSED("거래완료");

    private final String value;
}
