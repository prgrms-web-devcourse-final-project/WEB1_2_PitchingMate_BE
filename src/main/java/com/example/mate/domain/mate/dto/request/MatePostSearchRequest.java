package com.example.mate.domain.mate.dto.request;

import com.example.mate.entity.Age;
import com.example.mate.entity.Gender;
import com.example.mate.entity.TransportType;
import lombok.Getter;

@Getter
public class MatePostSearchRequest {
    private Age age;
    private Gender gender;
    private Integer maxParticipants;
    private TransportType transportType;
}
