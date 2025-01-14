package com.example.mate.domain.notification.integration;

import static org.hamcrest.Matchers.containsString;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.content;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.notification.repository.EmitterRepository;
import com.example.mate.domain.notification.repository.NotificationRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
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

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        member = createMember();
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
}
