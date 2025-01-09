package com.example.mate.domain.mateReview.dto.response;

import com.example.mate.domain.mateReview.entity.MateReview;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateReviewCreateResponse {
    private Long reviewId;
    private Long reviewerId;
    private Long revieweeId;
    private String revieweeNickName;
    private String content;
    private String rating;

    public static MateReviewCreateResponse from(MateReview review) {
        return MateReviewCreateResponse.builder()
                .reviewId(review.getId())
                .reviewerId(review.getReviewer().getId())
                .revieweeId(review.getReviewee().getId())
                .revieweeNickName(review.getReviewee().getNickname())
                .content(review.getReviewContent())
                .rating(review.getRating().getValue())
                .build();
    }
}
