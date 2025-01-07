package com.example.mate.domain.member.entity;

import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@RequiredArgsConstructor
public enum ActivityType {
    POST(0.001F),
    GOODS(0.002F),
    MATE(0.003F);

    private final Float value;
}
