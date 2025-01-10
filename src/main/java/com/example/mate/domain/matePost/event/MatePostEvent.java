package com.example.mate.domain.matePost.event;

import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.notification.entity.NotificationType;

public record MatePostEvent(Long matePostId, String matePostTitle, Member receiver, NotificationType notificationType) {

    public static MatePostEvent of(Long matePostId, String matePostTitle, Member receiver,
                                   NotificationType notificationType) {
        return new MatePostEvent(matePostId, matePostTitle, receiver, notificationType);
    }
}
