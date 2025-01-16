package com.example.mate.domain.notification.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.mockito.Mockito.verify;

import com.example.mate.common.response.PageResponse;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.notification.dto.response.NotificationResponse;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.repository.EmitterRepository;
import com.example.mate.domain.notification.repository.NotificationRepository;
import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import org.assertj.core.api.Assertions;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class NotificationServiceTest {

    @InjectMocks
    private NotificationService notificationService;

    @Mock
    private EmitterRepository emitterRepository;

    @Mock
    private NotificationRepository notificationRepository;

    @Mock
    private MemberRepository memberRepository;

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

    private Notification createTestNotification(NotificationType type, Member receiver) {
        return Notification.builder()
                .notificationType(type)
                .content("알림")
                .url("http://test.com")
                .receiver(receiver)
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

    @Nested
    @DisplayName("알림 페이징 조회")
    class NotificationPage {

        @Test
        @DisplayName("알림 페이징 조회 성공 - 전체 조회")
        void get_all_notifications_page_success() {
            // given
            Member member = createTestMember();
            Long memberId = 1L;
            String type = "all";
            Notification notification1 = createTestNotification(NotificationType.GOODS_CLOSED, member);
            Notification notification2 = createTestNotification(NotificationType.MATE_CLOSED, member);
            PageImpl<Notification> notificationPage = new PageImpl<>(List.of(notification1, notification2));
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(notificationRepository.findNotificationsByReceiverId(memberId, pageable))
                    .willReturn(notificationPage);

            // when
            PageResponse<NotificationResponse> response = notificationService.getNotificationsPage(type, memberId,
                    pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(notificationPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(notificationPage.getContent().size());

            NotificationResponse notificationResponse = response.getContent().get(0);
            assertThat(notificationResponse.getNotificationType()).isEqualTo(
                    notification1.getNotificationType().getValue());

            verify(memberRepository).findById(memberId);
            verify(notificationRepository).findNotificationsByReceiverId(memberId, pageable);
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 메이트 조회")
        void get_mate_notifications_page_success() {
            // given
            Member member = createTestMember();
            Long memberId = 1L;
            String type = "mate";
            Notification notification1 = createTestNotification(NotificationType.MATE_COMPLETE, member);
            Notification notification2 = createTestNotification(NotificationType.MATE_CLOSED, member);
            PageImpl<Notification> notificationPage = new PageImpl<>(List.of(notification1, notification2));
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(notificationRepository.findMateNotificationsByReceiverId(memberId, pageable))
                    .willReturn(notificationPage);

            // when
            PageResponse<NotificationResponse> response = notificationService.getNotificationsPage(type, memberId,
                    pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(notificationPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(notificationPage.getContent().size());

            NotificationResponse notificationResponse = response.getContent().get(0);
            assertThat(notificationResponse.getNotificationType()).isEqualTo(
                    notification1.getNotificationType().getValue());

            verify(memberRepository).findById(memberId);
            verify(notificationRepository).findMateNotificationsByReceiverId(memberId, pageable);
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 굿즈 조회")
        void get_goods_notifications_page_success() {
            // given
            Member member = createTestMember();
            Long memberId = 1L;
            String type = "goods";
            Notification notification1 = createTestNotification(NotificationType.GOODS_CLOSED, member);
            Notification notification2 = createTestNotification(NotificationType.GOODS_CLOSED, member);
            PageImpl<Notification> notificationPage = new PageImpl<>(List.of(notification1, notification2));
            Pageable pageable = PageRequest.of(0, 10);

            given(memberRepository.findById(memberId)).willReturn(Optional.of(member));
            given(notificationRepository.findGoodsNotificationsByReceiverId(memberId, pageable))
                    .willReturn(notificationPage);

            // when
            PageResponse<NotificationResponse> response = notificationService.getNotificationsPage(type, memberId,
                    pageable);

            // then
            assertThat(response).isNotNull();
            assertThat(response.getTotalElements()).isEqualTo(notificationPage.getTotalElements());
            assertThat(response.getContent().size()).isEqualTo(notificationPage.getContent().size());

            NotificationResponse notificationResponse = response.getContent().get(0);
            assertThat(notificationResponse.getNotificationType()).isEqualTo(
                    notification1.getNotificationType().getValue());

            verify(memberRepository).findById(memberId);
            verify(notificationRepository).findGoodsNotificationsByReceiverId(memberId, pageable);
        }
    }
}