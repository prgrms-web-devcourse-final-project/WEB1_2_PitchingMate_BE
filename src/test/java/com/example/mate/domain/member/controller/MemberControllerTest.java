package com.example.mate.domain.member.controller;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.member.dto.request.JoinRequest;
import com.example.mate.domain.member.dto.response.JoinResponse;
import com.example.mate.domain.member.dto.response.MemberProfileResponse;
import com.example.mate.domain.member.dto.response.MyProfileResponse;
import com.example.mate.domain.member.service.MemberService;
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

@WebMvcTest(MemberController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class MemberControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemberService memberService;

    private MyProfileResponse createMyProfileResponse() {
        return MyProfileResponse.builder()
                .nickname("tester")
                .imageUrl("default.jpg")
                .teamName("KIA")
                .manner(0.3f)
                .aboutMe("테스터입니다.")
                .followingCount(10)
                .followerCount(20)
                .reviewsCount(5)
                .goodsSoldCount(15)
                .goodsBoughtCount(12)
                .visitsCount(3)
                .build();
    }

    private MemberProfileResponse createMemberProfileResponse() {
        return MemberProfileResponse.builder()
                .nickname("tester")
                .imageUrl("default.jpg")
                .teamName("KIA")
                .manner(0.2f)
                .aboutMe("테스터입니다.")
                .followingCount(10)
                .followerCount(20)
                .reviewsCount(5)
                .goodsSoldCount(15)
                .build();
    }

    private JoinRequest createTestJoinRequest() {
        return JoinRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .gender("M")
                .birthyear("2000")
                .teamId(1L)
                .nickname("tester")
                .build();
    }

    private JoinResponse createTestJoinResponse() {
        return JoinResponse.builder()
                .name("홍길동")
                .nickname("tester")
                .email("test@example.com")
                .age(24)
                .gender("남자")
                .team("KIA")
                .build();
    }

    @Test
    @DisplayName("자체 회원 가입 - 성공")
    void join_success() throws Exception {
        // given
        JoinRequest joinRequest = createTestJoinRequest();
        JoinResponse joinResponse = createTestJoinResponse();

        given(memberService.join(any(JoinRequest.class))).willReturn(joinResponse);

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(joinRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.name").value("홍길동"))
                .andExpect(jsonPath("$.data.email").value("test@example.com"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andDo(print());
    }

    @Test
    @DisplayName("회원 가입 - teamId가 유효하지 않으면 400 오류 반환")
    void join_fail_team_id_invalid() throws Exception {
        // given
        JoinRequest invalidJoinRequest = JoinRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .gender("M")
                .birthyear("2000")
                .teamId(15L)  // 유효하지 않은 teamId
                .nickname("tester")
                .build();

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("teamId: teamId는 10 이하이어야 합니다."));
    }

    @Test
    @DisplayName("회원 가입 - nickname이 최대 길이(20자)를 초과하면 오류")
    void join_fail_invalid_nickname() throws Exception {
        // given
        JoinRequest invalidJoinRequest = JoinRequest.builder()
                .name("홍길동")
                .email("test@example.com")
                .gender("M")
                .birthyear("2000")
                .teamId(5L)
                .nickname("tester12345678901234567890")  // nickname 길이가 20자를 초과
                .build();

        // when & then
        mockMvc.perform(post("/api/members/join")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(invalidJoinRequest)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.message")
                        .value("nickname: nickname은 최대 20자까지 입력할 수 있습니다."));
    }

    @Test
    @DisplayName("내 프로필 조회 - 성공")
    void get_my_profile_success() throws Exception {
        // given
        Long memberId = 1L;
        MyProfileResponse response = createMyProfileResponse();

        given(memberService.getMyProfile(memberId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/members/me")
                        .param("memberId", memberId.toString()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                .andExpect(jsonPath("$.data.teamName").value("KIA"))
                .andExpect(jsonPath("$.data.manner").value(0.3))
                .andExpect(jsonPath("$.data.aboutMe").value("테스터입니다."))
                .andExpect(jsonPath("$.data.followingCount").value(10))
                .andExpect(jsonPath("$.data.followerCount").value(20))
                .andExpect(jsonPath("$.data.reviewsCount").value(5))
                .andExpect(jsonPath("$.data.goodsSoldCount").value(15))
                .andExpect(jsonPath("$.data.goodsBoughtCount").value(12))
                .andExpect(jsonPath("$.data.visitsCount").value(3))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로필 조회 - 회원이 존재하지 않는 경우")
    void get_my_profile_member_not_found() throws Exception {
        // given
        Long memberId = 999L;

        given(memberService.getMyProfile(memberId)).willThrow(
                new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        // when & then
        mockMvc.perform(get("/api/members/me")
                        .param("memberId", memberId.toString()))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("해당 ID의 회원 정보를 찾을 수 없습니다"))
                .andDo(print());
    }

    @Test
    @DisplayName("내 프로필 조회 - 잘못된 memberId 형식")
    void get_my_profile_invalid_member_id() throws Exception {
        // given
        String invalidMemberId = "invalid";  // 숫자가 아닌 잘못된 ID

        // when & then
        mockMvc.perform(get("/api/members/me")
                        .param("memberId", invalidMemberId))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("잘못된 입력 형식입니다. 'invalid' 타입이 'Long'로 변환될 수 없습니다."))
                .andDo(print());
    }

    @Test
    @DisplayName("다른 회원 프로필 조회")
    void find_member_info_success() throws Exception {
        // given
        Long memberId = 1L;
        MemberProfileResponse response = createMemberProfileResponse();

        given(memberService.getMemberProfile(memberId)).willReturn(response);

        // when & then
        mockMvc.perform(get("/api/members/{memberId}", memberId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data.nickname").value("tester"))
                .andExpect(jsonPath("$.data.imageUrl").value("default.jpg"))
                .andExpect(jsonPath("$.data.teamName").value("KIA"))
                .andExpect(jsonPath("$.data.manner").value(0.2))
                .andExpect(jsonPath("$.data.aboutMe").value("테스터입니다."))
                .andExpect(jsonPath("$.data.followingCount").value(10))
                .andExpect(jsonPath("$.data.followerCount").value(20))
                .andExpect(jsonPath("$.data.reviewsCount").value(5))
                .andExpect(jsonPath("$.data.goodsSoldCount").value(15));
    }
}
