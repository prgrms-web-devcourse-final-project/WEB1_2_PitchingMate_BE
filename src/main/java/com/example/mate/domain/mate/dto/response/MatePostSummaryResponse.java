package com.example.mate.domain.mate.dto.response;

import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatePostSummaryResponse {

    private String imageUrl;
    private String title;
    private Status status;
    private String rivalTeamName;
    private LocalDateTime rivalMatchTime;
    private String location;
    private Integer maxParticipants;
    private Age age;
    private Gender gender;
    private TransportType transportType;
}
