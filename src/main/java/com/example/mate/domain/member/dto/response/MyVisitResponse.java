package com.example.mate.domain.member.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.entity.MateReview;
import com.example.mate.domain.member.entity.Member;
import java.time.LocalDateTime;
import java.util.ArrayList;
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

    // 리뷰 정보
    @Builder.Default
    private List<MateReviewResponse> reviews = new ArrayList<>();

    @Getter
    @Builder
    @AllArgsConstructor(access = AccessLevel.PRIVATE)
    public static class MateReviewResponse {
        private Long memberId;
        private String nickname;
        private String rating;
        private String content;

        public static MateReviewResponse from(MateReview mateReview) {
            return MateReviewResponse.builder()
                    .memberId(mateReview.getReviewee().getId())
                    .nickname(mateReview.getReviewee().getNickname())
                    .rating(mateReview.getRating().getValue())
                    .content(mateReview.getReviewContent())
                    .build();
        }

        public static MateReviewResponse from(Member member) {
            return MateReviewResponse.builder()
                    .memberId(member.getId())
                    .nickname(member.getNickname())
                    .rating(null)
                    .content(null)
                    .build();
        }
    }

    public static MyVisitResponse of(Match match, List<MateReviewResponse> reviews) {
        return MyVisitResponse.builder()
                .homeTeamName(TeamInfo.getById(match.getHomeTeamId()).shortName)
                .awayTeamName(TeamInfo.getById(match.getAwayTeamId()).shortName)
                .location(match.getStadium().name)
                .matchTime(match.getMatchTime())
                .reviews(reviews)
                .build();
    }
}
