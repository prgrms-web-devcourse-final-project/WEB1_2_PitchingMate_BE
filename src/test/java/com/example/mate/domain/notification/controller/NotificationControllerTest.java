package com.example.mate.domain.notification.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.notification.dto.response.NotificationResponse;
import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.PageImpl;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.web.servlet.mvc.method.annotation.SseEmitter;

@WebMvcTest(NotificationController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember
class NotificationControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private NotificationService notificationService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    private static final Long DEFAULT_TIMEOUT = 60L * 1000 * 60;

    private NotificationResponse createNotificationResponse(Long notificationId, NotificationType type) {
        return NotificationResponse.builder()
                .notificationId(notificationId)
                .notificationType(type.getValue())
                .content("알림")
                .url("http://test.com")
                .isRead(false)
                .eventId(null)
                .build();
    }

    @Test
    @DisplayName("SSE 알림 구독 성공")
    void subscribe_success() throws Exception {
        // given
        Long memberId = 1L;
        String lastEventId = "";
        SseEmitter emitter = new SseEmitter(DEFAULT_TIMEOUT);

        // 404 에러 방지를 위해 SseEmitter에 더미 데이터 추가
        emitter.send(SseEmitter.event()
                .id("1")
                .name("test")
                .data("dummy event"));

        given(notificationService.subscribe(eq(memberId), eq(lastEventId))).willReturn(emitter);

        // when & then
        mockMvc.perform(get("/subscribe")
                        .header("Last-Event-ID", lastEventId)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(content().string("""
                        id:1
                        event:test
                        data:dummy event
                        
                        """));
    }

    @Nested
    @DisplayName("알림 페이징 조회")
    class NotificationPage {

        @Test
        @DisplayName("알림 페이징 조회 성공 - 전체 조회")
        void get_all_notifications_page_success() throws Exception {
            // given
            Long memberId = 1L;
            String type = "all";
            Pageable pageable = PageRequest.of(0, 10);
            NotificationResponse responseDTO1 = createNotificationResponse(1L, NotificationType.GOODS_CLOSED);
            NotificationResponse responseDTO2 = createNotificationResponse(2L, NotificationType.MATE_CLOSED);
            List<NotificationResponse> content = List.of(responseDTO2, responseDTO1);
            PageImpl<NotificationResponse> notificationPage = new PageImpl<>(content);

            PageResponse<NotificationResponse> response = PageResponse.<NotificationResponse>builder()
                    .content(content)
                    .totalPages(notificationPage.getTotalPages())
                    .totalElements(notificationPage.getTotalElements())
                    .hasNext(notificationPage.hasNext())
                    .pageNumber(notificationPage.getNumber())
                    .pageSize(notificationPage.getSize())
                    .build();

            given(notificationService.getNotificationsPage(type, memberId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(content.size()))
                    .andExpect(jsonPath("$.data.content[0].notificationId").value(responseDTO2.getNotificationId()))
                    .andExpect(jsonPath("$.data.content[0].notificationType").value(responseDTO2.getNotificationType()))
                    .andExpect(jsonPath("$.data.totalPages").value(response.getTotalPages()))
                    .andExpect(jsonPath("$.data.totalElements").value(response.getTotalElements()))
                    .andExpect(jsonPath("$.data.pageNumber").value(response.getPageNumber()))
                    .andExpect(jsonPath("$.data.pageSize").value(response.getPageSize()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 메이트 조회")
        void get_mate_notifications_page_success() throws Exception {
            // given
            Long memberId = 1L;
            String type = "mate";
            Pageable pageable = PageRequest.of(0, 10);
            NotificationResponse responseDTO1 = createNotificationResponse(1L, NotificationType.MATE_COMPLETE);
            NotificationResponse responseDTO2 = createNotificationResponse(2L, NotificationType.MATE_CLOSED);
            List<NotificationResponse> content = List.of(responseDTO2, responseDTO1);
            PageImpl<NotificationResponse> notificationPage = new PageImpl<>(content);

            PageResponse<NotificationResponse> response = PageResponse.<NotificationResponse>builder()
                    .content(content)
                    .totalPages(notificationPage.getTotalPages())
                    .totalElements(notificationPage.getTotalElements())
                    .hasNext(notificationPage.hasNext())
                    .pageNumber(notificationPage.getNumber())
                    .pageSize(notificationPage.getSize())
                    .build();

            given(notificationService.getNotificationsPage(type, memberId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("type", type)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(content.size()))
                    .andExpect(jsonPath("$.data.content[0].notificationId").value(responseDTO2.getNotificationId()))
                    .andExpect(jsonPath("$.data.content[0].notificationType").value(responseDTO2.getNotificationType()))
                    .andExpect(jsonPath("$.data.totalPages").value(response.getTotalPages()))
                    .andExpect(jsonPath("$.data.totalElements").value(response.getTotalElements()))
                    .andExpect(jsonPath("$.data.pageNumber").value(response.getPageNumber()))
                    .andExpect(jsonPath("$.data.pageSize").value(response.getPageSize()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 굿즈 조회")
        void get_goods_notifications_page_success() throws Exception {
            // given
            Long memberId = 1L;
            String type = "mate";
            Pageable pageable = PageRequest.of(0, 10);
            NotificationResponse responseDTO1 = createNotificationResponse(1L, NotificationType.GOODS_CLOSED);
            NotificationResponse responseDTO2 = createNotificationResponse(2L, NotificationType.GOODS_CLOSED);
            List<NotificationResponse> content = List.of(responseDTO2, responseDTO1);
            PageImpl<NotificationResponse> notificationPage = new PageImpl<>(content);

            PageResponse<NotificationResponse> response = PageResponse.<NotificationResponse>builder()
                    .content(content)
                    .totalPages(notificationPage.getTotalPages())
                    .totalElements(notificationPage.getTotalElements())
                    .hasNext(notificationPage.hasNext())
                    .pageNumber(notificationPage.getNumber())
                    .pageSize(notificationPage.getSize())
                    .build();

            given(notificationService.getNotificationsPage(type, memberId, pageable)).willReturn(response);

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("type", type)
                            .param("page", String.valueOf(pageable.getPageNumber()))
                            .param("size", String.valueOf(pageable.getPageSize())))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(content.size()))
                    .andExpect(jsonPath("$.data.content[0].notificationId").value(responseDTO2.getNotificationId()))
                    .andExpect(jsonPath("$.data.content[0].notificationType").value(responseDTO2.getNotificationType()))
                    .andExpect(jsonPath("$.data.totalPages").value(response.getTotalPages()))
                    .andExpect(jsonPath("$.data.totalElements").value(response.getTotalElements()))
                    .andExpect(jsonPath("$.data.pageNumber").value(response.getPageNumber()))
                    .andExpect(jsonPath("$.data.pageSize").value(response.getPageSize()))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }
}
