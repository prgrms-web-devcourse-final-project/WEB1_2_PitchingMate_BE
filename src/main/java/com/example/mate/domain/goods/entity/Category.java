package com.example.mate.domain.goods.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Category {

    UNIFORM("유니폼"),
    CAP("모자"),
    CLOTHING("의류"),
    ACCESSORY("잡화"),
    SOUVENIR("기념상품");

    private final String value;
}
