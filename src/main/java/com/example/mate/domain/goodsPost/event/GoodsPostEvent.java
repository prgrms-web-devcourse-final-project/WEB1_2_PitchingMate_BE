package com.example.mate.domain.goodsPost.event;

import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.notification.entity.NotificationType;

public record GoodsPostEvent(Long goodsPostId, String goodsPostTitle, Member receiver,
                             NotificationType notificationType) {

    public static GoodsPostEvent of(Long goodsPostId, String goodsPostTitle, Member receiver,
                                    NotificationType notificationType) {
        return new GoodsPostEvent(goodsPostId, goodsPostTitle, receiver, notificationType);
    }
}
