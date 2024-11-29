package com.example.mate.domain.member.integration;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.example.mate.domain.member.service.MemberService;
import com.fasterxml.jackson.databind.ObjectMapper;
import jakarta.transaction.Transactional;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MemberIntegrationTest {

    private static final Logger log = LoggerFactory.getLogger(MemberIntegrationTest.class);
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MemberService memberService;

    private Member member;

    @BeforeEach
    void setUp() {
        memberRepository.deleteAll();

        // 데이터베이스에 테스트 회원을 삽입
        member = Member.builder()
                .name("홍길동")
                .nickname("tester")
                .email("tester@example.com")
                .imageUrl("default.jpg")
                .age(20)
                .gender(Gender.MALE)
                .teamId(1L)
                .manner(0.300F)
                .aboutMe("tester입니다.")
                .build();
        memberRepository.save(member);
    }

    private JoinRequest createTestJoinRequest() {
        return JoinRequest.builder()
                .name("김철수")
                .email("tester2@example.com")
                .gender("M")
                .birthyear("2002")
                .teamId(1L)
                .nickname("tester2")
                .build();
    }

    @Test
    @DisplayName("자체 회원 가입 - 성공")
    void join_success() throws Exception {
        // given
        JoinRequest joinRequest = createTestJoinRequest();

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("김철수"))
                .andExpect(jsonPath("$.data.email").value("tester2@example.com"))
                .andExpect(jsonPath("$.data.age").value(22))
                .andExpect(jsonPath("$.data.nickname").value("tester2"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 - teamId가 유효하지 않으면 오류")
    void join_fail_invalid_teamId() throws Exception {
        // given
        JoinRequest invalidJoinRequest = JoinRequest.builder()
                .name("김철수")
                .email("tester2@example.com")
                .gender("M")
                .birthyear("2002")
                .teamId(15L)  // 유효하지 않은 teamId
                .nickname("tester2")
                .build();

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                .andExpect(status().isBadRequest())  // 400 오류가 반환되기를 기대
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("teamId: teamId는 10 이하이어야 합니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 - nickname이 최대 길이(20자)를 초과하면 오류")
    void join_fail_invalid_nickname() throws Exception {
        // given
        JoinRequest invalidJoinRequest = JoinRequest.builder()
                .name("김철수")
                .email("tester2@example.com")
                .gender("M")
                .birthyear("2002")
                .teamId(1L)
                .nickname("tester12345678901234567890")  // nickname 길이가 20자를 초과
                .build();

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                .andExpect(status().isBadRequest())  // 400 오류가 반환되기를 기대
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("nickname: nickname은 최대 20자까지 입력할 수 있습니다."))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());
    }


    @Test
    @DisplayName("다른 회원 프로필 조회 - 성공")
    void find_member_info_success() throws Exception {
        // when & then
        mockMvc.perform(get("/api/members/" + member.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                .andExpect(jsonPath("$.data.teamName").value("KIA"))
                .andExpect(jsonPath("$.data.aboutMe").value("tester입니다."))
                .andExpect(jsonPath("$.data.manner").value(0.300F))
                .andDo(print());
    }

    @Test
    @DisplayName("다른 회원 프로필 조회 - 실패 (해당 회원 없음)")
    void find_member_info_fail_member_not_found() throws Exception {
        // given
        long invalidMemberId = member.getId() + 999L;

        // when & then
        mockMvc.perform(get("/api/members/" + invalidMemberId))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"))
                .andExpect(jsonPath("$.code").value(404))
                .andDo(print());
    }
}
