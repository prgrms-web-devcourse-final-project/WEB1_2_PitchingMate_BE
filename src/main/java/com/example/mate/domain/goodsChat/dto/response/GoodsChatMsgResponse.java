package com.example.mate.domain.goodsChat.dto.response;

import com.example.mate.domain.goodsChat.entity.GoodsChatMessage;
import com.example.mate.domain.goodsChat.entity.GoodsChatPart;
import com.example.mate.domain.member.entity.Member;
import java.time.LocalDateTime;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsChatMsgResponse {

    private final Long chatMessageId;
    private final Long authorId;
    private final String content;
    private final String authorImageUrl;
    private final LocalDateTime sentAt;

    public static GoodsChatMsgResponse of(GoodsChatMessage chatMessage) {
        GoodsChatPart goodsChatPart = chatMessage.getGoodsChatPart();
        Member author = goodsChatPart.getMember();

        return GoodsChatMsgResponse.builder()
                .chatMessageId(chatMessage.getId())
                .authorId(author.getId())
                .authorImageUrl(author.getImageUrl())
                .content(chatMessage.getContent())
                .sentAt(chatMessage.getSentAt())
                .build();
    }
}
