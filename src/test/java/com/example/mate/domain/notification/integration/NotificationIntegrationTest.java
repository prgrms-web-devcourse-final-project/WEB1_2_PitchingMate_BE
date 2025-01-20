package com.example.mate.domain.notification.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.notification.entity.Notification;
import com.example.mate.domain.notification.entity.NotificationType;
import com.example.mate.domain.notification.repository.EmitterRepository;
import com.example.mate.domain.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class NotificationIntegrationTest {

    @Autowired
    private MockMvc mockMvc;
    @Autowired
    private MemberRepository memberRepository;
    @Autowired
    private EmitterRepository emitterRepository;
    @Autowired
    private NotificationRepository notificationRepository;
    @Autowired
    private JdbcTemplate jdbcTemplate;
    @Autowired
    private ObjectMapper objectMapper;

    private Member member;
    private Notification notification1;
    private Notification notification2;
    private Notification notification3;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");
        jdbcTemplate.execute("ALTER TABLE notification ALTER COLUMN id RESTART WITH 1");

        member = createMember();
        notification1 = createNotification(NotificationType.GOODS_CLOSED);
        notification2 = createNotification(NotificationType.MATE_CLOSED);
        notification3 = createNotification(NotificationType.MATE_COMPLETE);
    }

    @Test
    @DisplayName("SSE 알림 구독 성공")
    @WithAuthMember
    void subscribe_success() throws Exception {
        // given
        Long memberId = member.getId();
        String lastEventId = "";

        // when & then
        mockMvc.perform(get("/subscribe")
                        .header("Last-Event-ID", lastEventId)
                        .accept(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(status().isOk())
                .andExpect(content().contentTypeCompatibleWith(MediaType.TEXT_EVENT_STREAM_VALUE))
                .andExpect(content().string(containsString("data:EventStream Created. [userId=" + memberId + "]")));
    }

    @Nested
    @DisplayName("알림 페이징 조회")
    class NotificationPage {

        @Test
        @DisplayName("알림 페이징 조회 성공 - 전체 조회")
        @WithAuthMember
        void get_all_notifications_page_success() throws Exception {
            // given
            Long memberId = member.getId();
            String type = "all";
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("type", type)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(3))
                    .andExpect(jsonPath("$.data.content[0].notificationId").value(3))
                    .andExpect(jsonPath("$.data.content[0].notificationType").value(
                            NotificationType.MATE_COMPLETE.getValue()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 메이트 조회")
        @WithAuthMember
        void get_mate_notifications_page_success() throws Exception {
            // given
            Long memberId = member.getId();
            String type = "mate";
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("type", type)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(2))
                    .andExpect(jsonPath("$.data.content[1].notificationId").value(2))
                    .andExpect(jsonPath("$.data.content[1].notificationType").value(
                            NotificationType.MATE_CLOSED.getValue()))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("알림 페이징 조회 성공 - 굿즈 조회")
        @WithAuthMember
        void get_goods_notifications_page_success() throws Exception {
            // given
            Long memberId = member.getId();
            String type = "goods";
            int page = 0;
            int size = 10;

            // when & then
            mockMvc.perform(get("/api/notifications")
                            .param("type", type)
                            .param("page", String.valueOf(page))
                            .param("size", String.valueOf(size)))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content.size()").value(1))
                    .andExpect(jsonPath("$.data.content[0].notificationId").value(1))
                    .andExpect(jsonPath("$.data.content[0].notificationType").value(
                            NotificationType.GOODS_CLOSED.getValue()))
                    .andExpect(jsonPath("$.code").value(200));
        }
    }

    private Member createMember() {
        return memberRepository.save(Member.builder()
                .name("홍길동")
                .email("test@gmail.com")
                .nickname("테스터")
                .imageUrl("upload/test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private Notification createNotification(NotificationType type) {
        return notificationRepository.save(Notification.builder()
                .notificationType(type)
                .content("알림")
                .url("http://test.com")
                .receiver(member)
                .build());
    }
}
