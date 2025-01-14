package com.example.mate.domain.notification.service;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.repository.EmitterRepository;
import com.example.mate.domain.notification.repository.NotificationRepository;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private NotificationRepository notificationRepository;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private Member createTestMember() {
        return Member.builder()
                .id(1L)
                .imageUrl("image.png")
                .name("홍길동")
                .nickname("tester")
                .imageUrl("image.png")
                .email("test@example.com")
                .age(30)
                .gender(Gender.MALE)
                .teamId(1L)
                .manner(0.300F)
                .build();
    }


    @Test
    @DisplayName("알림 구독 성공")
    void subscribe_success() {
        // given
        Member member = createTestMember();
        String lastEventId = "";
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        given(emitterRepository.save(any(String.class), any(SseEmitter.class))).willAnswer(invocation -> {
            String actualEmitterId = invocation.getArgument(0); // 첫 번째 인자 캡처
            Assertions.assertThat(actualEmitterId).startsWith(member.getId() + "_");
            return emitter;
        });

        // when
        SseEmitter response = notificationService.subscribe(member.getId(), lastEventId);

        // then
        Assertions.assertThat(response).isNotNull();
        verify(emitterRepository).save(any(String.class), any(SseEmitter.class));
    }

    @Test
    @DisplayName("알림 메시지 전송 성공")
    void send_success() {
        // given
        Member member = createTestMember();
        NotificationType notificationType = NotificationType.MATE_CLOSED;
        String content = "모집이 완료되었습니다.";
        String url = "http://example.com";
        Notification notification = Notification.builder()
                .notificationType(notificationType)
                .content(content)
                .url(url)
                .receiver(member)
                .build();

        String eventId = member.getId() + "_123456789";
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);
        Map<String, SseEmitter> emitters = new ConcurrentHashMap<>();
        emitters.put(eventId, emitter);

        given(emitterRepository.findAllEmitterStartsWithMemberId(any(String.class))).willReturn(emitters);
        given(notificationRepository.save(any(Notification.class))).willReturn(notification);

        // when
        notificationService.send(notificationType, content, url, member);

        // then
        verify(notificationRepository).save(any(Notification.class));
        verify(emitterRepository).findAllEmitterStartsWithMemberId(String.valueOf(member.getId()));
        verify(emitterRepository).saveEventCache(eventId, notification);
    }
}