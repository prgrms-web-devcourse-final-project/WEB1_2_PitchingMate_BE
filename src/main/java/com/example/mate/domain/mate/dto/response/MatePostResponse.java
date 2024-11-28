package com.example.mate.domain.mate.dto.response;

import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatePostResponse {
    private Long id;
    private Status status;

    public static MatePostResponse from(MatePost matePost) {
        return MatePostResponse.builder().id(matePost.getId()).status(matePost.getStatus()).build();
    }
}
