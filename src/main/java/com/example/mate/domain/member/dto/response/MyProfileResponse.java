package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.member.entity.Member;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyProfileResponse {

    @Schema(description = "사용자 닉네임", example = "tester")
    private String nickname;

    @Schema(description = "사용자의 프로필 이미지 URL", example = "/images/default.png")
    private String imageUrl;

    @Schema(description = "사용자 응원팀 이름", example = "KIA")
    private String teamName;

    @Schema(description = "매너 타율", example = "0.300")
    private Float manner;

    @Schema(description = "소개글", example = "안녕하세요.")
    private String aboutMe;

    @Schema(description = "사용자가 팔로잉한 사람 수", example = "120")
    private Integer followingCount;

    @Schema(description = "사용자를 팔로우한 사람 수", example = "200")
    private Integer followerCount;

    @Schema(description = "사용자가 받은 총 리뷰 수", example = "15")
    private Integer reviewsCount;

    @Schema(description = "사용자가 판매한 상품의 개수", example = "8")
    private Integer goodsSoldCount;

    @Schema(description = "사용자가 구매한 상품의 개수", example = "12")
    private Integer goodsBoughtCount;

    @Schema(description = "사용자의 직관 수", example = "5")
    private Integer visitsCount;

    public static MyProfileResponse of(Member member, int followingCount, int followerCount, int reviewsCount,
                                       int goodsSoldCount, int goodsBoughtCount, int visitsCount) {
        return MyProfileResponse.builder()
                .nickname(member.getNickname())
                .imageUrl(FileUtils.getThumbnailImageUrl(member.getImageUrl()))
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

    public static MyProfileResponse from(Member member) {
        return MyProfileResponse.builder()
                .nickname(member.getNickname())
                .imageUrl(FileUtils.getThumbnailImageUrl(member.getImageUrl()))
                .teamName(TeamInfo.getById(member.getTeamId()).shortName)
                .aboutMe(member.getAboutMe())
                .build();
    }
}
