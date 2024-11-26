package com.example.mate.domain.mate.entity;

import lombok.Getter;

@Getter
public enum TransportType {
    ANY("상관없음"),
    PUBLIC("대중교통"),
    CAR("자차"),
    CARPOOL("카풀");

    private final String value;

    TransportType(String value) {
        this.value = value;
    }
}
