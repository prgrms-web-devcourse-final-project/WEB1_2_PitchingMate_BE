package com.example.mate.domain.mate.dto.response;

import com.example.mate.entity.Age;
import com.example.mate.entity.Gender;
import com.example.mate.entity.Status;
import com.example.mate.entity.TransportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatePostDetailResponse {
    private String postImageUrl;
    private String title;
    private Status status;
    private String rivalTeamName;
    private LocalDateTime rivalMatchTime;
    private String location;
    private Age age;
    private Gender gender;
    private TransportType transportType;
    private Integer maxParticipants;
    private String userImageUrl;
    private String nickname;
    private Float manner;
    private String description;
    private Long postId;
}
