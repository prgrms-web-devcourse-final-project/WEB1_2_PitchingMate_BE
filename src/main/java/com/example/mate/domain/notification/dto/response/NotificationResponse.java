package com.example.mate.domain.notification.dto.response;

import com.example.mate.domain.notification.entity.Notification;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class NotificationResponse {

    @Schema(description = "알림 ID", example = "1")
    private Long notificationId;

    @Schema(description = "알림 유형", example = "모집완료")
    private String notificationType;

    @Schema(description = "알림 내용", example = "굿즈 거래 후기를 남겨주세요!")
    private String content;

    @Schema(description = "해당 게시글 Url", example = "http://example.com")
    private String url;

    @Schema(description = "알림 확인 여부", example = "false")
    private Boolean isRead;

    @Schema(description = "이벤트 ID", example = "1_1736919132500")
    private String eventId;

    public static NotificationResponse of(Notification notification, String eventId) {
        return NotificationResponse.builder()
                .notificationId(notification.getId())
                .notificationType(notification.getNotificationType().getValue())
                .content(notification.getContent())
                .url(notification.getUrl())
                .isRead(notification.getIsRead())
                .eventId(eventId)
                .build();
    }
}
