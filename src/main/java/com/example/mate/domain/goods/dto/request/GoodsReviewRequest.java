package com.example.mate.domain.goods.dto.request;

import com.example.mate.common.validator.ValidEnum;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsReview;
import com.example.mate.domain.member.entity.Member;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public class GoodsReviewRequest {

    @ValidEnum(message = "평점의 입력 값이 잘못되었습니다.", enumClass = Rating.class)
    private Rating rating;

    @Size(max = 100, message = "리뷰 내용은 최대 100자까지 입력 가능합니다.")
    private String reviewContent;

    public GoodsReview toEntity(GoodsPost goodsPost, Member reviewer, Member reviewee) {
        return GoodsReview.builder()
                .reviewer(reviewer)
                .reviewee(reviewee)
                .goodsPost(goodsPost)
                .rating(rating)
                .reviewContent(reviewContent)
                .build();
    }
}
