package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.member.entity.Member;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MemberSummaryResponse {

    private Long memberId;
    private String nickname;
    private String imageUrl;
    private String teamName;

    public static MemberSummaryResponse from(Member member) {
        return MemberSummaryResponse.builder()
                .memberId(member.getId())
                .nickname(member.getNickname())
                .imageUrl(FileUtils.getThumbnailImageUrl(member.getImageUrl()))
                .teamName(TeamInfo.getById(member.getTeamId()).shortName)
                .build();
    }
}
