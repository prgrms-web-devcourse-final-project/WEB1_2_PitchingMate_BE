package com.example.mate.domain.goods.dto.response;

import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsReviewFormResponse {

    private final Long goodsPostId;
    private final String goodsPostTitle;
    private final String reviewer;
    private final String reviewee;
    private final String imageUrl;
}
