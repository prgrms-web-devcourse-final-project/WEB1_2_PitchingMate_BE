package com.example.mate.domain.goods.dto.response;

import com.example.mate.domain.constant.Rating;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsReviewResponse {

    private final Long id;
    private final Rating rating;
    private final String reviewContent;
    private final String reviewer;
    private final Long revieweeId;
    private final LocalDateTime createdAt;
    private final Long goodsPostId;
    private final String goodsPostTitle;
}
