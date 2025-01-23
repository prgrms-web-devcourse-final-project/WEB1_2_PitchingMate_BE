package com.example.mate.domain.member.controller;


import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.BDDMockito.given;
import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.PageResponse;
import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.config.securityConfig.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.dto.response.MemberSummaryResponse;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.service.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FollowController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
@WithAuthMember(userId = "customUser", memberId = 1L)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FollowService followService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    private MemberSummaryResponse createMemberSummaryResponse() {
        return MemberSummaryResponse.builder()
                .nickname("tester1")
                .memberId(1L)
                .imageUrl("tester1.png")
                .teamName("KIA")
                .build();
    }

    @Nested
    @DisplayName("회원 팔로우 테스트")
    class FollowMember {

        private Member createMember(Long id) {
            return Member.builder()
                    .id(id)
                    .name("홍길동" + id)
                    .nickname("tester" + id)
                    .email("tester" + id + "example.com")
                    .imageUrl("image" + id + ".png")
                    .age((int) (20 + id))
                    .gender(Gender.MALE)
                    .teamId(1L)
                    .manner(0.300F)
                    .aboutMe("this is tester")
                    .build();
        }

        private Follow createFollow(Member follower, Member following) {
            return Follow.builder().id(1L).follower(follower).following(following).build();
        }

        @Test
        @DisplayName("다른 회원 팔로우 성공")
        void follow_member_success() throws Exception {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            willDoNothing().given(followService).follow(followerId, followingId);

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isOk())
                    .andDo(print());

            verify(followService, times(1)).follow(followerId, followingId);
        }

        @Test
        @DisplayName("이미 팔로우한 회원을 다시 팔로우하려는 경우 예외 발생")
        void follow_member_already_followed() throws Exception {
            // given
            Long followerId = 1L;
            Long followingId = 2L;

            // 팔로우가 이미 되어 있다고 설정
            willThrow(new CustomException(ErrorCode.ALREADY_FOLLOWED_MEMBER))
                    .given(followService).follow(followerId, followingId);

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isBadRequest())  // 400 Bad Request
                    .andDo(print());

            verify(followService, times(1)).follow(followerId, followingId);
        }

        @Test
        @DisplayName("존재하지 않는 팔로워 또는 팔로잉을 팔로우하려는 경우 예외 발생")
        void follow_member_not_found() throws Exception {
            // given
            Long followerId = 1L;
            Long followingId = 999L; // 존재하지 않는 팔로잉 ID

            // 존재하지 않는 팔로워 또는 팔로잉에 대한 예외 설정
            willThrow(new CustomException(ErrorCode.FOLLOWER_NOT_FOUND_BY_ID))
                    .given(followService).follow(followerId, followingId);

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isNotFound())  // 404 Not Found
                    .andDo(print());

            verify(followService, times(1)).follow(followerId, followingId);
        }
    }

    @Nested
    @DisplayName("회원 언팔로우 테스트")
    class UnfollowMember {

        @Test
        @DisplayName("다른 회원 언팔로우 성공")
        void unfollow_member_success() throws Exception {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 2L;

            willDoNothing().given(followService).unfollow(unfollowerId, unfollowingId);

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            verify(followService, times(1)).unfollow(unfollowerId, unfollowingId);
        }

        @Test
        @DisplayName("이미 언팔로우한 회원을 다시 언팔로우하려는 경우 예외 발생")
        void unfollow_member_already_unfollowed() throws Exception {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 2L;

            // 언팔로우가 이미 되어 있다고 설정
            willThrow(new CustomException(ErrorCode.ALREADY_UNFOLLOWED_MEMBER))
                    .given(followService).unfollow(unfollowerId, unfollowingId);

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isBadRequest())
                    .andDo(print());

            verify(followService, times(1)).unfollow(unfollowerId, unfollowingId);
        }

        @Test
        @DisplayName("존재하지 않는 언팔로워 또는 언팔로잉을 언팔로우하려는 경우 예외 발생")
        void unfollow_member_not_found() throws Exception {
            // given
            Long unfollowerId = 1L;
            Long unfollowingId = 999L; // 존재하지 않는 팔로잉 ID

            // 존재하지 않는 언팔로워 또는 언팔로잉에 대한 예외 설정
            willThrow(new CustomException(ErrorCode.UNFOLLOWER_NOT_FOUND_BY_ID))
                    .given(followService).unfollow(unfollowerId, unfollowingId);

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isNotFound())
                    .andDo(print());

            verify(followService, times(1)).unfollow(unfollowerId, unfollowingId);
        }
    }

    @Nested
    @DisplayName("팔로우 리스트 페이징")
    class FollowingPage {

        @Test
        @DisplayName("팔로우 리스트 페이징 성공")
        void get_followings_page_success() throws Exception {
            // given
            Long memberId = 2L;
            PageResponse<MemberSummaryResponse> responses = PageResponse.<MemberSummaryResponse>builder()
                    .content(List.of(createMemberSummaryResponse()))
                    .totalPages(1)
                    .totalElements(1L)
                    .hasNext(false)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            given(followService.getFollowingsPage(eq(memberId), any(Pageable.class))).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followings", memberId)
                            .param("page", "1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("팔로우 리스트 페이징 실패 - 해당 회원이 없는 경우")
        void get_followings_page_member_not_found() throws Exception {
            // given
            Long memberId = 999L;  // 존재하지 않는 회원 ID

            given(followService.getFollowingsPage(eq(memberId), any(Pageable.class)))
                    .willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followings", memberId)
                            .param("page", "1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(
                            ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage()));
        }
    }

    @Nested
    @DisplayName("팔로워 리스트 페이징")
    class FollowerPage {

        @Test
        @DisplayName("팔로워 리스트 페이징 성공")
        void get_followers_page_success() throws Exception {
            // given
            Long memberId = 2L;
            PageResponse<MemberSummaryResponse> responses = PageResponse.<MemberSummaryResponse>builder()
                    .content(List.of(createMemberSummaryResponse()))
                    .totalPages(1)
                    .totalElements(1L)
                    .hasNext(false)
                    .pageNumber(0)
                    .pageSize(10)
                    .build();

            given(followService.getFollowersPage(eq(memberId), any(Pageable.class))).willReturn(responses);

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followers", memberId)
                            .param("page", "1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("팔로워 리스트 페이징 실패 - 해당 회원이 없는 경우")
        void get_followers_page_member_not_found() throws Exception {
            // given
            Long memberId = 999L;  // 존재하지 않는 회원 ID

            given(followService.getFollowersPage(eq(memberId), any(Pageable.class)))
                    .willThrow(new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followers", memberId)
                            .param("page", "1")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.code").value(404))
                    .andExpect(jsonPath("$.message").value(
                            ErrorCode.MEMBER_NOT_FOUND_BY_ID.getMessage()));
        }
    }
}