package com.example.mate.domain.goods.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Status {

    OPEN("거래중"),
    CLOSED("거래완료");

    private final String value;
}
