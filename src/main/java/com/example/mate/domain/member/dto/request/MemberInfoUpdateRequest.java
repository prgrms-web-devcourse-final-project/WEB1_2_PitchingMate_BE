package com.example.mate.domain.member.dto.request;

import lombok.Getter;

@Getter
public class MemberInfoUpdateRequest {

    private Long teamId;
    private String nickname;
    private String aboutMe;
}
