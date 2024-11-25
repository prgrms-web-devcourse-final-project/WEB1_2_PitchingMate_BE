package com.example.mate.domain.mate.dto.request;

import lombok.Getter;

import java.util.List;

@Getter
public class MatePostCompleteRequest {
    private List<Long> participantIds;
}
