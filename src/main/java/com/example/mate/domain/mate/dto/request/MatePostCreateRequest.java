package com.example.mate.domain.mate.dto.request;

import com.example.mate.entity.Age;
import com.example.mate.entity.Gender;
import com.example.mate.entity.TransportType;
import lombok.Getter;

@Getter
public class MatePostCreateRequest {
    private Long teamId;
    private Long matchId;
    private String title;
    private String content;
    private Age age;
    private Integer maxParticipants;
    private Gender gender;
    private TransportType transportType;
}