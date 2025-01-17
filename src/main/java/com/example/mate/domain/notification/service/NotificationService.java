package com.example.mate.domain.notification.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.notification.dto.response.NotificationResponse;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.repository.EmitterRepository;
import com.example.mate.domain.notification.repository.NotificationRepository;
import java.io.IOException;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@Service
@RequiredArgsConstructor
@Transactional
public class NotificationService {

    private final EmitterRepository emitterRepository;
    private final NotificationRepository notificationRepository;
    private final MemberRepository memberRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60; // 연결 지속시간 1시간

    // 회원 알림 구독
    public SseEmitter subscribe(Long memberId, String lastEventId) {
        String emitterId = makeIdWithTime(memberId);
        SseEmitter emitter = emitterRepository.save(emitterId, new SseEmitter(DEFAULT_TIMEOUT));

        // 비동기 요청, 타임아웃 후 리소스 정리
        emitter.onCompletion(() -> emitterRepository.deleteById(emitterId));
        emitter.onTimeout(() -> emitterRepository.deleteById(emitterId));

        // 더미 이벤트 생성 -> 503 방지
        String eventId = makeIdWithTime(memberId);
        sendNotification(emitter, eventId, emitterId, "EventStream Created. [userId=" + memberId + "]");

        // 클라이언트가 미수신한 이벤트 목록이 존재할 경우 모두 전송 -> 이벤트 유실 방지
        if (hasLostData(lastEventId)) {
            sendLostData(lastEventId, memberId, emitterId, emitter);
        }

        return emitter;
    }

    private String makeIdWithTime(Long memberId) {
        return memberId + "_" + System.currentTimeMillis();
    }

    private void sendNotification(SseEmitter emitter, String eventId, String emitterId, Object data) {
        try {
            emitter.send(SseEmitter.event()
                    .id(eventId)
                    .data(data));
        } catch (IOException e) {
            emitterRepository.deleteById(emitterId);
            throw new CustomException(ErrorCode.NOTIFICATION_SEND_ERROR);
        }
    }

    private boolean hasLostData(String lastEventId) {
        return !lastEventId.isEmpty();
    }

    private void sendLostData(String lastEventId, Long memberId, String emitterId, SseEmitter emitter) {
        Map<String, Object> eventCaches = emitterRepository
                .findAllEventCacheStartsWithMemberId(String.valueOf(memberId));
        eventCaches.entrySet().stream()
                .filter(entry -> lastEventId.compareTo(entry.getKey()) < 0)
                .forEach(entry -> sendNotification(emitter, entry.getKey(), emitterId, entry.getValue()));
    }

    // 알림 메시지 전송
    public void send(NotificationType notificationType, String content, String url, Member receiver) {
        Notification notification = notificationRepository.save(
                new Notification(notificationType, content, url, receiver));
        String eventId = makeIdWithTime(receiver.getId());

        Map<String, SseEmitter> emitters = emitterRepository.findAllEmitterStartsWithMemberId(
                String.valueOf(receiver.getId()));
        emitters.forEach((key, emitter) -> {
            emitterRepository.saveEventCache(key, notification);
            sendNotification(emitter, eventId, key, NotificationResponse.of(notification, eventId));
        });
    }

    // 알림 페이징 조회
    @Transactional(readOnly = true)
    public PageResponse<NotificationResponse> getNotificationsPage(String type, Long memberId, Pageable pageable) {
        validateMemberId(memberId);

        Page<Notification> notificationsPage;
        switch (type) {
            case "mate" ->
                    notificationsPage = notificationRepository.findMateNotificationsByReceiverId(memberId, pageable);
            case "goods" ->
                    notificationsPage = notificationRepository.findGoodsNotificationsByReceiverId(memberId, pageable);
            default -> notificationsPage = notificationRepository.findNotificationsByReceiverId(memberId, pageable);
        }
        List<NotificationResponse> content = notificationsPage.getContent().stream()
                .map(notification -> NotificationResponse.of(notification, null))
                .toList();

        return PageResponse.from(notificationsPage, content);
    }

    private void validateMemberId(Long memberId) {
        memberRepository.findById(memberId)
                .orElseThrow(() -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    // 회원 알림 읽음 처리
    public void readNotification(Long memberId, Long notificationId) {
        Notification notification = validateNotification(memberId, notificationId);
        notification.changeIsRead(true);
    }

    private Notification validateNotification(Long memberId, Long notificationId) {
        Notification notification = notificationRepository.findById(notificationId)
                .orElseThrow(() -> new CustomException(ErrorCode.NOTIFICATION_NOT_FOUND));

        if (!Objects.equals(notification.getReceiver().getId(), memberId)) {
            throw new CustomException(ErrorCode.INVALID_RECEIVER);
        }
        return notification;
    }
}
