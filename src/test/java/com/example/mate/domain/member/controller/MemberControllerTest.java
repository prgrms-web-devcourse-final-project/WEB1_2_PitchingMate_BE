package com.example.mate.domain.member.controller;

import static org.mockito.BDDMockito.given;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.member.dto.response.MemberProfileResponse;
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

    private MemberProfileResponse createMemberProfileResponse() {
        return MemberProfileResponse.builder()
                .nickname("tester")
                .imageUrl("default.jpg")
                .teamName("KIA")
                .manner(4.5f)
                .aboutMe("테스터입니다.")
                .followingCount(10)
                .followerCount(20)
                .reviewsCount(5)
                .goodsSoldCount(15)
                .build();
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
                .andExpect(jsonPath("$.data.manner").value(4.5))
                .andExpect(jsonPath("$.data.aboutMe").value("테스터입니다."))
                .andExpect(jsonPath("$.data.followingCount").value(10))
                .andExpect(jsonPath("$.data.followerCount").value(20))
                .andExpect(jsonPath("$.data.reviewsCount").value(5))
                .andExpect(jsonPath("$.data.goodsSoldCount").value(15));
    }
}
