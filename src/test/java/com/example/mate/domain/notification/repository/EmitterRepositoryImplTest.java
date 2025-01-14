package com.example.mate.domain.notification.repository;

import static org.assertj.core.api.Assertions.assertThat;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import java.util.Map;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@ExtendWith(MockitoExtension.class)
class EmitterRepositoryImplTest {

    @InjectMocks
    private EmitterRepository emitterRepository = new EmitterRepositoryImpl();

    private Long DEFAULT_TIMEOUT = 60L * 1000L * 60L;

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
    @DisplayName("SseEmitter 저장 성공")
    void save_sse_emitter_success() throws Exception {
        // given
        Long memberId = 1L;
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        // when
        SseEmitter savedEmitter = emitterRepository.save(emitterId, sseEmitter);

        // then
        assertNotNull(savedEmitter);
        assertEquals(sseEmitter, savedEmitter);
    }

    @Test
    @DisplayName("수신한 이벤트를 캐시에 저장 성공")
    void save_event_cache_success() throws Exception {
        // given
        Long memberId = 1L;
        String eventCacheId = memberId + "_" + System.currentTimeMillis();
        Notification notification = Notification.builder()
                .notificationType(NotificationType.MATE_CLOSED)
                .content("메이트 모집에 참여되었습니다.")
                .url("url")
                .receiver(createTestMember())
                .build();

        // when & then
        assertDoesNotThrow(() -> emitterRepository.saveEventCache(eventCacheId, notification));
        Object savedEvent = emitterRepository.findAllEventCacheStartsWithMemberId("1").get(eventCacheId);
        assertNotNull(savedEvent);
        assertEquals(notification, savedEvent);
    }

    @Test
    @DisplayName("특정 회원의 모든 SseEmitter 조회 성공")
    public void find_all_emitter_starts_with_member_id_success() throws Exception {
        //given
        Long memberId = 1L;
        String emitterId1 = memberId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId2 = memberId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId3 = memberId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId3, new SseEmitter(DEFAULT_TIMEOUT));

        // when
        Map<String, SseEmitter> savedEmitters = emitterRepository
                .findAllEmitterStartsWithMemberId(String.valueOf(memberId));

        // then
        assertThat(savedEmitters.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("특정 회원에의 모든 이벤트를 캐시에서 조회 성공")
    public void find_all_event_cache_starts_with_member_id_success() throws Exception {
        //given
        Member member = createTestMember();
        String eventCacheId1 = member.getId() + "_" + System.currentTimeMillis();
        Notification notification1 = Notification.builder()
                .notificationType(NotificationType.MATE_CLOSED)
                .content("메이트 모집에 참여되었습니다.")
                .url("url")
                .receiver(member)
                .build();
        emitterRepository.saveEventCache(eventCacheId1, notification1);

        Thread.sleep(100);
        String eventCacheId2 = member.getId() + "_" + System.currentTimeMillis();
        Notification notification2 = Notification.builder()
                .notificationType(NotificationType.MATE_COMPLETE)
                .content("직관 후기를 남겨주세요!")
                .url("url")
                .receiver(member)
                .build();
        emitterRepository.saveEventCache(eventCacheId2, notification2);

        Thread.sleep(100);
        String eventCacheId3 = member.getId() + "_" + System.currentTimeMillis();
        Notification notification3 = Notification.builder()
                .notificationType(NotificationType.GOODS_CLOSED)
                .content("{판매자}에게 굿즈 거래 후기를 남겨주세요!")
                .url("url")
                .receiver(member)
                .build();
        emitterRepository.saveEventCache(eventCacheId3, notification3);

        //when
        Map<String, Object> savedEventCaches = emitterRepository
                .findAllEventCacheStartsWithMemberId(String.valueOf(member.getId()));

        //then
        assertThat(savedEventCaches.size()).isEqualTo(3);
    }

    @Test
    @DisplayName("ID를 통해 SseEmitter 삭제 성공")
    public void delete_by_id_success() throws Exception {
        //given
        Long memberId = 1L;
        String emitterId = memberId + "_" + System.currentTimeMillis();
        SseEmitter sseEmitter = new SseEmitter(DEFAULT_TIMEOUT);

        //when
        emitterRepository.save(emitterId, sseEmitter);
        emitterRepository.deleteById(emitterId);

        //then
        assertThat(emitterRepository.findAllEmitterStartsWithMemberId(String.valueOf(memberId)).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 회원의 모든 SseEmitter 삭제 성공")
    public void delete_all_emitters_starts_with_member_id_success() throws Exception {
        //given
        Long memberId = 1L;
        String emitterId1 = memberId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId1, new SseEmitter(DEFAULT_TIMEOUT));

        Thread.sleep(100);
        String emitterId2 = memberId + "_" + System.currentTimeMillis();
        emitterRepository.save(emitterId2, new SseEmitter(DEFAULT_TIMEOUT));

        //when
        emitterRepository.deleteAllEmitterStartsWithMemberId(String.valueOf(memberId));

        //then
        assertThat(emitterRepository.findAllEmitterStartsWithMemberId(String.valueOf(memberId)).size()).isEqualTo(0);
    }

    @Test
    @DisplayName("특정 회원의 모든 이벤트를 캐시에서 삭제 성공")
    public void delete_all_event_cache_starts_with_member_id_success() throws Exception {
        //given
        Member member = createTestMember();
        String eventCacheId1 = member.getId() + "_" + System.currentTimeMillis();
        Notification notification1 = Notification.builder()
                .notificationType(NotificationType.MATE_CLOSED)
                .content("메이트 모집에 참여되었습니다.")
                .url("url")
                .receiver(member)
                .build();
        emitterRepository.saveEventCache(eventCacheId1, notification1);

        Thread.sleep(100);
        String eventCacheId2 = member.getId() + "_" + System.currentTimeMillis();
        Notification notification2 = Notification.builder()
                .notificationType(NotificationType.MATE_COMPLETE)
                .content("직관 후기를 남겨주세요!")
                .url("url")
                .receiver(member)
                .build();
        emitterRepository.saveEventCache(eventCacheId2, notification2);

        //when
        emitterRepository.deleteAllEventCacheStartWithMemberId(String.valueOf(member.getId()));

        //then
        assertThat(emitterRepository.findAllEventCacheStartsWithMemberId(String.valueOf(member.getId())).size())
                .isEqualTo(0);
    }
}