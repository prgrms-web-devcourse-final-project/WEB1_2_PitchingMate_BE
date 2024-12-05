package com.example.mate.domain.goodsChat.dto.response;

import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.entity.GoodsPost;
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
    private final String postStatus;
    private final String chatRoomStatus;
    private final String imageUrl;

    private final PageResponse<GoodsChatMessageResponse> initialMessages;

    public static GoodsChatRoomResponse of(GoodsChatRoom chatRoom, PageResponse<GoodsChatMessageResponse> initialMessages) {
        GoodsPost goodsPost = chatRoom.getGoodsPost();
        String mainImageUrl = goodsPost.getMainImageUrl();
        String teamName = getTeamName(goodsPost);

        return GoodsChatRoomResponse.builder()
                .chatRoomId(chatRoom.getId())
                .goodsPostId(goodsPost.getId())
                .teamName(teamName)
                .title(goodsPost.getTitle())
                .category(goodsPost.getCategory().getValue())
                .price(goodsPost.getPrice())
                .imageUrl(mainImageUrl)
                .postStatus(goodsPost.getStatus().getValue())
                .chatRoomStatus(chatRoom.getIsActive().toString())
                .initialMessages(initialMessages)
                .build();
    }

    private static String getTeamName(GoodsPost goodsPost) {
        return goodsPost.getTeamId() == null ? null : TeamInfo.getById(goodsPost.getTeamId()).shortName;
    }
}
