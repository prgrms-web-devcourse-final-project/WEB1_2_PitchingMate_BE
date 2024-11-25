package com.example.mate.domain.member.dto.response;

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

    public static MemberSummaryResponse from() {
        return MemberSummaryResponse.builder()
                .memberId(1L)
                .nickname("홍길동")
                .imageUrl("upload/default.jpg")
                .build();
    }
}
