package com.example.mate.domain.goods.dto.request;

import lombok.Getter;

@Getter
public class GoodsReviewFormRequest {

    private String goodsPostTitle;
    private String reviewer;
    private String reviewee;
    private String imageUrl;
}
