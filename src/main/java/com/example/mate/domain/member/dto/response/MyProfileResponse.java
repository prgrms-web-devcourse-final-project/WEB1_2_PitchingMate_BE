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
public class MyProfileResponse {

    private String nickname;
    private String imageUrl;
    private String teamName;
    private Float manner;
    private String aboutMe;

    private Integer followingCount;
    private Integer followerCount;
    private Integer reviewsCount;
    private Integer goodsSoldCount;
    private Integer goodsBoughtCount;
    private Integer visitsCount;

    public static MyProfileResponse of(Member member, int followingCount, int followerCount, int reviewsCount,
                                       int goodsSoldCount, int goodsBoughtCount, int visitsCount) {
        return MyProfileResponse.builder()
                .nickname(member.getNickname())
                .imageUrl(member.getImageUrl())
                .teamName(TeamInfo.getById(member.getTeamId()).shortName)
                .manner(member.getManner())
                .aboutMe(member.getAboutMe())
                .followingCount(followingCount)
                .followerCount(followerCount)
                .reviewsCount(reviewsCount)
                .goodsSoldCount(goodsSoldCount)
                .goodsBoughtCount(goodsBoughtCount)
                .visitsCount(visitsCount)
                .build();
    }
}
