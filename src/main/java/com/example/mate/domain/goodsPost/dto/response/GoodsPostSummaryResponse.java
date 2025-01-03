package com.example.mate.domain.goodsPost.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsPostSummaryResponse {

    private final Long id;
    private final String teamName;
    private final String title;
    private final String category;
    private final Integer price;
    private final String imageUrl;

    public static GoodsPostSummaryResponse of(GoodsPost goodsPost, String imageFileName) {
        String teamName = goodsPost.getTeamId() == null ? null : TeamInfo.getById(goodsPost.getTeamId()).shortName;

        return GoodsPostSummaryResponse.builder()
                .id(goodsPost.getId())
                .teamName(teamName)
                .title(goodsPost.getTitle())
                .category(goodsPost.getCategory().getValue())
                .price(goodsPost.getPrice())
                .imageUrl(FileUtils.getThumbnailImageUrl(imageFileName))
                .build();
    }
}
