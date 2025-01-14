package com.example.mate.domain.notification.controller;

import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.notification.service.NotificationService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
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
}
