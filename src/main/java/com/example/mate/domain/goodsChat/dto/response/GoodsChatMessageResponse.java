package com.example.mate.domain.goodsChat.dto.response;

import com.example.mate.domain.file.FileUtils;
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
public class GoodsChatMessageResponse {

    private final Long chatMessageId;
    private final Long roomId;
    private final Long senderId;
    private final String senderNickname;
    private final String message;
    private final String messageType;
    private final String senderImageUrl;
    private final LocalDateTime sentAt;

    public static GoodsChatMessageResponse of(GoodsChatMessage chatMessage) {
        GoodsChatPart goodsChatPart = chatMessage.getGoodsChatPart();
        Member sender = goodsChatPart.getMember();

        return GoodsChatMessageResponse.builder()
                .chatMessageId(chatMessage.getId())
                .roomId(goodsChatPart.getGoodsChatRoom().getId())
                .senderId(sender.getId())
                .senderNickname(sender.getNickname())
                .senderImageUrl(FileUtils.getThumbnailImageUrl(sender.getImageUrl()))
                .message(chatMessage.getContent())
                .messageType(chatMessage.getMessageType().getValue())
                .sentAt(chatMessage.getSentAt())
                .build();
    }
}
