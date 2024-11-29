package com.example.mate.domain.member.controller;


import static org.mockito.BDDMockito.willDoNothing;
import static org.mockito.BDDMockito.willThrow;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.service.FollowService;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.test.web.servlet.MockMvc;

@WebMvcTest(FollowController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class FollowControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private FollowService followService;

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
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
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
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
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
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
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
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId)
                            .param("unfollowerId", String.valueOf(unfollowerId)))
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
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId)
                            .param("unfollowerId", String.valueOf(unfollowerId)))
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
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId)
                            .param("unfollowerId", String.valueOf(unfollowerId)))
                    .andExpect(status().isNotFound())
                    .andDo(print());

            verify(followService, times(1)).unfollow(unfollowerId, unfollowingId);
        }
    }

}