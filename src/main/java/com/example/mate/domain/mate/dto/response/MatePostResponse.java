package com.example.mate.domain.mate.dto.response;

import com.example.mate.entity.Status;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MatePostResponse {
    private Long id;
    private Status status;
}
