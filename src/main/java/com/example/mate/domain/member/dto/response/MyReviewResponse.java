package com.example.mate.domain.member.dto.response;

import java.time.LocalDateTime;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class MyReviewResponse {
    private Long postId;
    private String title;
    private String nickname;
    private String rating;
    private String content;
    private LocalDateTime created_at;

    public static MyReviewResponse goodsFrom() {
        return MyReviewResponse.builder()
                .postId(1L)
                .title("쁘띠 이대호 피규어 & 쁘띠 이대호 빠따")
                .nickname("볼빨간사촌형")
                .rating("좋았어요!")
                .content("물건 너무 이쁘고 보존 상태가 좋아요!")
                .created_at(LocalDateTime.now().minusDays(3))
                .build();
    }

    public static MyReviewResponse mateFrom() {
        return MyReviewResponse.builder()
                .postId(1L)
                .title("11/21일 삼성 vs 기아 직관가실분")
                .nickname("서대문박병호")
                .rating("좋았어요!")
                .content("야구 지식이 풍부하시고 젠틀하십니다!")
                .created_at(LocalDateTime.now().minusDays(3))
                .build();
    }
}

