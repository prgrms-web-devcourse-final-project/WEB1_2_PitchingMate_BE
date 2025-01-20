package com.example.mate.domain.notification.repository;

import java.util.Map;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

public interface EmitterRepository {

    // Emitter 저장
    SseEmitter save(String emitterId, SseEmitter sseEmitter);

    // 이벤트 저장
    void saveEventCache(String eventCacheId, Object event);

    // 특정 회원과 관련된 모든 Emitter 조회
    Map<String, SseEmitter> findAllEmitterStartsWithMemberId(String memberId);

    // 특정 회원과 관련된 모든 이벤트 조회
    Map<String, Object> findAllEventCacheStartsWithMemberId(String memberId);

    // Emitter 삭제
    void deleteById(String id);

    // 특정 회원과 관련된 모든 Emitter 삭제
    void deleteAllEmitterStartsWithMemberId(String memberId);

    // 특정 회원과 관련된 모든 이벤트 삭제
    void deleteAllEventCacheStartWithMemberId(String memberId);
}
