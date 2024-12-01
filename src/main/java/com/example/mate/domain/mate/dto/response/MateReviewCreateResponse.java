package com.example.mate.domain.mate.dto.response;

import com.example.mate.domain.mate.entity.MateReview;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateReviewCreateResponse {
    private Long reviewId;
    private Long reviewerId;
    private Long revieweeId;
    private String revieweeName;
    private String content;
    private String rating;

    public static MateReviewCreateResponse from(MateReview review) {
        return MateReviewCreateResponse.builder()
                .reviewId(review.getId())
                .reviewerId(review.getReviewer().getId())
                .revieweeId(review.getReviewee().getId())
                .revieweeName(review.getReviewee().getName())
                .content(review.getReviewContent())
                .rating(review.getRating().getValue())
                .build();
    }
}
