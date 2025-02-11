package com.example.mate.domain.goodsPost.entity;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;
import java.util.Arrays;
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

    @JsonValue
    private final String value;

    @JsonCreator
    public static Category from(String value) {
        return Arrays.stream(values())
                .filter(category -> category.getValue().equals(value))
                .findAny()
                .orElse(null);
    }
}
