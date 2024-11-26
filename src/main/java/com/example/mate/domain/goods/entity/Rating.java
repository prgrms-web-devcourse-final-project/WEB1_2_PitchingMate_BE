package com.example.mate.domain.goods.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Rating {

    BAD("별로예요"),
    GOOD("좋아요!"),
    GREAT("최고예요!");

    private final String value;
}
