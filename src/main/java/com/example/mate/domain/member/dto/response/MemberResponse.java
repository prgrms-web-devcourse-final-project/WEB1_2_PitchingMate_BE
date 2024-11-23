package com.example.mate.domain.member.dto.response;

import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberResponse {

    private String nickname;
    private String imageUrl;
    private String team;
    private Float manner;
    private String aboutMe;
    private Integer followingCount;
    private Integer followerCount;
}
