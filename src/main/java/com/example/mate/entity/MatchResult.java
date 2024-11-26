package com.example.mate.entity;

import lombok.Getter;

@Getter
public enum MatchResult {
    WIN("승"),
    LOSE("패"),
    DRAW("무");

    private final String description;

    MatchResult(String description) {
        this.description = description;
    }
}