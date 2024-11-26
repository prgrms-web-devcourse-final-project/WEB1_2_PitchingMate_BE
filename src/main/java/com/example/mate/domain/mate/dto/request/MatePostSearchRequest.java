package com.example.mate.domain.mate.dto.request;

import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.TransportType;
import lombok.Getter;

@Getter
public class MatePostSearchRequest {
    private Age age;
    private Gender gender;
    private Integer maxParticipants;
    private TransportType transportType;
}
