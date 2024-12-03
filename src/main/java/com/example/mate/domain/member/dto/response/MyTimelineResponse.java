package com.example.mate.domain.member.dto.response;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor
public class MyTimelineResponse {

    private Long visitId;
    private Long matePostId;
    private Long memberId;
}
