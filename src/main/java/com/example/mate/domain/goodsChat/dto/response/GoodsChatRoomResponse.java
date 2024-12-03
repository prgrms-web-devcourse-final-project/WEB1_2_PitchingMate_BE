package com.example.mate.domain.goodsChat.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsChatRoomResponse {

    private final Long chatRoomId;
    private final Long goodsPostId;
    private final String teamName;
    private final String title;
    private final String category;
    private final Integer price;
    private final String status;
    private final String imageUrl;

    private static final String DEFAULT_IMAGE_URL = "upload/default.jpg";

    public static GoodsChatRoomResponse of(GoodsChatRoom chatRoom) {
        GoodsPost goodsPost = chatRoom.getGoodsPost();
        String mainImageUrl = getMainImageUrl(goodsPost);
        String teamName = getTeamName(goodsPost);

        return GoodsChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .goodsPostId(goodsPost.getId())
                .teamName(teamName)
                .title(goodsPost.getTitle())
                .category(goodsPost.getCategory().getValue())
                .price(goodsPost.getPrice())
                .imageUrl(mainImageUrl)
                .status(goodsPost.getStatus().getValue())
                .build();
    }

    private static String getMainImageUrl(GoodsPost goodsPost) {
        return goodsPost.getGoodsPostImages().stream()
                .filter(GoodsPostImage::getIsMainImage)
                .findFirst()
                .map(GoodsPostImage::getImageUrl)
                .orElse(DEFAULT_IMAGE_URL);
    }

    private static String getTeamName(GoodsPost goodsPost) {
        return goodsPost.getTeamId() == null ? null : TeamInfo.getById(goodsPost.getTeamId()).shortName;
    }
}