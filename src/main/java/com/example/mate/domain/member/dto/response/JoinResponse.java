package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class JoinResponse {

    private Long memberId;
    private String name;
    private String nickname;
    private String email;
    private Integer age;
    private String gender;
    private String team;

    public static JoinResponse from(Member member) {
        return JoinResponse.builder()
                .memberId(member.getId())
                .name(member.getName())
                .nickname(member.getNickname())
                .email(member.getEmail())
                .age(member.getAge())
                .gender(member.getGender().getValue())
                .team(TeamInfo.getById(member.getTeamId()).shortName)
                .build();
    }
}
