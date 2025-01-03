package com.example.mate.domain.goodsChat.dto.response;

import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsChat.entity.GoodsChatRoom;
import com.example.mate.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsChatRoomSummaryResponse {

    private final Long chatRoomId;
    private final String opponentNickname;
    private final String lastChatContent;
    private final LocalDateTime lastChatSentAt;
    private final String placeName;
    private final String goodsMainImageUrl;
    private final String opponentImageUrl;

    public static GoodsChatRoomSummaryResponse of(GoodsChatRoom chatRoom, Member opponent) {
        GoodsPost goodsPost = chatRoom.getGoodsPost();

        return GoodsChatRoomSummaryResponse.builder()
                .chatRoomId(chatRoom.getId())
                .opponentNickname(opponent.getNickname())
                .lastChatContent(chatRoom.getLastChatContent())
                .lastChatSentAt(chatRoom.getLastChatSentAt())
                .placeName(goodsPost.getLocation().getPlaceName())
                .goodsMainImageUrl(FileUtils.getThumbnailImageUrl(goodsPost.getMainImageUrl()))
                .opponentImageUrl(FileUtils.getThumbnailImageUrl(opponent.getImageUrl()))
                .build();
    }
}
