package com.example.mate.domain.member.dto.response;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyVisitResponse {
    private String homeTeamName;
    private String awayTeamName;
    private String location;
    private LocalDate matchTime;
    private LocalDate createdAt;
    private List<MateReviewResponse> reviews;

    public static MyVisitResponse from() {
        return MyVisitResponse.builder()
                .homeTeamName("삼성")
                .awayTeamName("KT")
                .location("대구 라이온스 파크")
                .matchTime(LocalDate.now().minusDays(10))
                .createdAt(LocalDate.now().minusDays(3))
                .reviews(Collections.nCopies(3, MateReviewResponse.from()))
                .build();
    }

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MateReviewResponse {
        private Long memberId;
        private String nickname;
        private String rating;
        private String content;

        private static MateReviewResponse from() {
            return MateReviewResponse.builder()
                    .memberId(1L)
                    .nickname("김아무개")
                    .rating("좋았어요!")
                    .content("정말 재미있는 직관이었어요. 함께해서 즐거웠습니다.")
                    .build();
        }
    }
}
