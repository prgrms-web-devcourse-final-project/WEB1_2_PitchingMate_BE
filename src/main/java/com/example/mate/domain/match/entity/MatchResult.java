package com.example.mate.domain.match.entity;

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