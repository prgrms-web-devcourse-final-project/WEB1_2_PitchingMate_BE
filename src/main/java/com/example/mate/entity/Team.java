package com.example.mate.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum Team {

    KIA(1L, "KIA", "KIA 타이거즈", "광주-기아 챔피언스 필드"),
    LG(2L, "LG", "LG 트윈스", "잠실야구장"),
    NC(3L, "NC", "NC 다이노스", "창원NC파크"),
    SSG(4L, "SSG", "SSG 랜더스", "인천SSG랜더스필드"),
    KT(5L, "KT", "kt wiz", "수원 kt wiz 파크"),
    DOOSAN(6L, "두산", "두산 베어스", "잠실야구장"),
    LOTTE(7L, "롯데", "롯데 자이언츠", "사직야구장"),
    SAMSUNG(8L, "삼성", "삼성 라이온즈", "대구삼성라이온즈파크"),
    KIWOOM(9L, "키움", "키움 히어로즈", "고척스카이돔"),
    HANWHA(10L, "한화", "한화 이글스", "한화생명이글스파크");

    private final Long id;
    private final String shortName;
    private final String fullName;
    private final String stadium;
}
