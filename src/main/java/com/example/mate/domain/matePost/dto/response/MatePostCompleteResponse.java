package com.example.mate.domain.matePost.dto.response;

import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Status;
import lombok.Builder;
import lombok.Getter;

import java.util.List;

@Getter
@Builder
public class MatePostCompleteResponse {

    private Long id;
    private Status status;
    private List<Long> participantIds;

    public static MatePostCompleteResponse from(MatePost matePost) {
        return MatePostCompleteResponse.builder()
                .id(matePost.getId())
                .status(matePost.getStatus())
                .participantIds(matePost.getVisit().getParticipants().stream()
                        .map(visitPart -> visitPart.getMember().getId())
                        .toList())
                .build();
    }
}
