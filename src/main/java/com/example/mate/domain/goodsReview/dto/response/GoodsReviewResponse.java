package com.example.mate.domain.goodsReview.dto.response;

import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goodsReview.entity.GoodsReview;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsReviewResponse {

    private final Long reviewId;
    private final String reviewerNickname;
    private final Rating rating;
    private final String reviewContent;
    private final LocalDateTime createdAt;

    private final Long goodsPostId;
    private final String goodsPostTitle;

    public static GoodsReviewResponse of(GoodsReview review) {
        return GoodsReviewResponse.builder()
                .reviewId(review.getId())
                .reviewerNickname(review.getReviewer().getNickname())
                .rating(review.getRating())
                .reviewContent(review.getReviewContent())
                .createdAt(review.getCreatedAt())
                .goodsPostId(review.getGoodsPost().getId())
                .goodsPostTitle(review.getGoodsPost().getTitle())
                .build();
    }
}
