package com.example.mate.domain.notification.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.auth.AuthMember;
import com.example.mate.common.validator.ValidPageable;
import com.example.mate.domain.notification.dto.response.NotificationResponse;
import com.example.mate.domain.notification.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Pageable;
import org.springframework.http.MediaType;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@RestController
@RequiredArgsConstructor
@Tag(name = "Notification Controller", description = "회원 알림 관련 API")
public class NotificationController {

    private final NotificationService notificationService;

    @Operation(summary = "알림 구독", description = "알림을 구독합니다.")
    @GetMapping(value = "/subscribe", produces = MediaType.TEXT_EVENT_STREAM_VALUE)
    public ResponseEntity<SseEmitter> subscribe(
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember,
            @RequestHeader(value = "Last-Event-ID", required = false, defaultValue = "") String lastEventId) {
        return ResponseEntity.ok(notificationService.subscribe(authMember.getMemberId(), lastEventId));
    }

    @Operation(summary = "알림 페이징 조회", description = "알림 페이지에서 전체/메이트 찾기/굿즈 거래 알림을 조회합니다.")
    @GetMapping("/api/notifications")
    public ResponseEntity<ApiResponse<PageResponse<NotificationResponse>>> getNotifications(
            @Parameter(description = "알림 분류") @RequestParam(required = false, defaultValue = "all") String type,
            @Parameter(description = "회원 로그인 정보") @AuthenticationPrincipal AuthMember authMember,
            @Parameter(description = "페이징 정보", required = true) @ValidPageable Pageable pageable
    ) {
        PageResponse<NotificationResponse> response = notificationService.getNotificationsPage(type,
                authMember.getMemberId(), pageable);
        return ResponseEntity.ok(ApiResponse.success(response));
    }
}
