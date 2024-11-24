package com.example.mate.domain.members.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Team {

    KIA(1L, "KIA"),
    LG(2L, "LG"),
    NC(3L, "NC"),
    SSG(4L, "SSG"),
    KT(5L, "KT"),
    DOOSAN(6L, "두산"),
    LOTTE(7L, "롯데"),
    SAMSUNG(8L, "삼성"),
    KIWOOM(9L, "키움"),
    HANWHA(10L, "한화"),
    ALL(11L, "전체");

    private final Long id;
    private final String value;
}
