package com.example.mate.domain.mate.dto.response;

import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Getter
@Builder
public class MateTimeLineResponse {
    private LocalDateTime visitDate;
    private String stadiumName;
    private String teamName;
    private String rivalTeamName;
    private String imageUrl;
    private List<TimelineParticipantResponse> participants;
}
