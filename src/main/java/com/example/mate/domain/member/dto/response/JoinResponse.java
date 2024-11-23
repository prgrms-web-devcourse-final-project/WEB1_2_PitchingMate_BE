package com.example.mate.domain.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JoinResponse {

    private String name;
    private String nickname;
    private String email;
    private Integer age;
    private String gender;
    private String team;
}
