package com.example.mate.domain.mate.dto.response;

import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class TimelineParticipantResponse {
    private Long userId;
    private String nickname;
    private String content;
}
