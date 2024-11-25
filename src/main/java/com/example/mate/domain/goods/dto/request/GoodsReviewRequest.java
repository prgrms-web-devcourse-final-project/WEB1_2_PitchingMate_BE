package com.example.mate.domain.goods.dto.request;

import com.example.mate.domain.goods.entity.Rating;
import lombok.Getter;

@Getter
public class GoodsReviewRequest {

    private Long reviewerId;
    private Rating rating;
    private String reviewContent;
}