package com.example.mate.domain.goodsPost.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.GoodsPostImage;
import com.example.mate.domain.goodsPost.entity.Role;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsPostResponse {

    private final Long id;
    private final MemberInfo seller;
    private final MemberInfo buyer;
    private final String teamName;
    private final String title;
    private final String category;
    private final Integer price;
    private final String content;
    private final LocationInfo location;
    private final List<String> imageUrls;
    private final String status;

    public static GoodsPostResponse of(GoodsPost goodsPost) {
        String teamName = goodsPost.getTeamId() == null ? null : TeamInfo.getById(goodsPost.getTeamId()).shortName;

        return GoodsPostResponse.builder()
                .id(goodsPost.getId())
                .seller(MemberInfo.from(goodsPost.getSeller(), Role.SELLER))
                .teamName(teamName)
                .title(goodsPost.getTitle())
                .category(goodsPost.getCategory().getValue())
                .price(goodsPost.getPrice())
                .content(goodsPost.getContent())
                .location(LocationInfo.from(goodsPost.getLocation()))
                .imageUrls(goodsPost.getGoodsPostImages().stream().map(GoodsPostImage::getImageUrl).toList())
                .status(goodsPost.getStatus().getValue())
                .build();
    }
}
