package com.example.mate.domain.goodsPost.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Role {

    SELLER("판매자"),
    BUYER("구매자");

    private final String value;
}
