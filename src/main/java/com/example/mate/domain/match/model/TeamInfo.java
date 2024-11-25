package com.example.mate.domain.match.model;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class TeamInfo {
    private Long id;
    private final String shortName;
    private final String fullName;
    private String stadium;
}