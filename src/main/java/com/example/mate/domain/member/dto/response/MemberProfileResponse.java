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
public class MemberProfileResponse {

    private String nickname;
    private String imageUrl;
    private String teamName;
    private Float manner;
    private String aboutMe;

    private Integer followingCount;
    private Integer followerCount;
    private Integer reviewsCount;
    private Integer goodsSoldCount;

    public static MemberProfileResponse of(Member member, int followingCount, int followerCount,
                                           int reviewsCount, int goodsSoldCount) {
        return MemberProfileResponse.builder()
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .teamName(TeamInfo.getById(member.getTeamId()).shortName)
                .manner(member.getManner())
                .aboutMe(member.getAboutMe())
                .followingCount(followingCount)
                .followerCount(followerCount)
                .reviewsCount(reviewsCount)
                .goodsSoldCount(goodsSoldCount)
                .build();
    }
}
