package com.example.mate.domain.member.dto.response;

import java.time.LocalDateTime;
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
    // 경기 정보
    private String homeTeamName;
    private String awayTeamName;
    private String location;
    private LocalDateTime matchTime;

    // 구인글 정보
    private Long postId;
    private String imageUrl;
    private String title;

    // 리뷰 정보
    private List<MateReviewResponse> reviews;

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

    public static MyVisitResponse from() {
        return MyVisitResponse.builder()
                .homeTeamName("삼성")
                .awayTeamName("KT")
                .location("대구 라이온스 파크")
                .matchTime(LocalDateTime.now().minusDays(10))
                .postId(1L)
                .imageUrl("upload/default.jpg")
                .title("구인 게시글 제목!")
                .reviews(Collections.nCopies(3, MateReviewResponse.from()))
                .build();
    }
}
