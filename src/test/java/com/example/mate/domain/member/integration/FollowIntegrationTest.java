package com.example.mate.domain.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.error.ErrorCode;
import com.example.mate.config.WithAuthMember;
import com.example.mate.common.security.util.JwtUtil;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
public class FollowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private MemberRepository memberRepository;

    @MockBean
    private JwtUtil jwtUtil;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member member1;
    private Member member2;
    private Follow follow;

    @BeforeEach
    void setUp() {
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY FALSE");
        jdbcTemplate.execute("TRUNCATE TABLE member");
        jdbcTemplate.execute("SET REFERENTIAL_INTEGRITY TRUE");

        followRepository.deleteAll();
        memberRepository.deleteAll();

        createMember();
        createFollow(member1, member2);  // member1만 member2 팔로우한 상황 설정
    }

    private void createMember() {
        member1 = Member.builder()
                .name("홍길동")
                .nickname("tester1")
                .email("tester1@example.com")
                .imageUrl("image.png")
                .age(20)
                .gender(Gender.MALE)
                .teamId(1L)
                .manner(0.300F)
                .aboutMe("this is tester")
                .build();
        memberRepository.save(member1);
        member2 = Member.builder()
                .name("김영희")
                .nickname("tester2")
                .email("tester2@example.com")
                .imageUrl("image.png")
                .age(20)
                .gender(Gender.FEMALE)
                .teamId(1L)
                .manner(0.300F)
                .aboutMe("this is tester")
                .build();
        memberRepository.save(member2);
    }

    private void createFollow(Member follower, Member following) {
        follow = Follow.builder().follower(follower).following(following).build();
        followRepository.save(follow);
    }

    @Nested
    @DisplayName("회원 팔로우")
    class FollowMember {

        @Test
        @DisplayName("회원 팔로우 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void follow_member_success() throws Exception {
            // given
            Long followerId = member2.getId();
            Long followingId = member1.getId();

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isOk())
                    .andDo(print());

            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(2); // 기존 1개 + 새로 1개 추가
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollower().getId()).isEqualTo(followingId);
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollowing().getId()).isEqualTo(followerId);
        }

        @Test
        @DisplayName("회원 팔로우 실패 -이미 팔로우한 회원을 다시 팔로우하려는 경우 예외 발생")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void follow_member_already_followed() throws Exception {
            // given - 이미 팔로우 되어 있는 상태인 member1가 member2 팔로우 상황 가정
            Long followerId = member1.getId();
            Long followingId = member2.getId();

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("이미 팔로우한 회원입니다."))
                    .andDo(print());

            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(1);
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollower().getId()).isEqualTo(member1.getId());
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollowing().getId()).isEqualTo(member2.getId());
        }

        @Test
        @DisplayName("회원 팔로우 실패 - 존재하지 않는 팔로워 또는 팔로잉을 팔로우하려는 경우 예외 발생")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void follow_member_not_found() throws Exception {
            // given
            Long followingId = member1.getId() + 999L;

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 ID의 팔로잉 회원을 찾을 수 없습니다."))
                    .andDo(print());

            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(1);
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollower().getId()).isEqualTo(member1.getId());
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollowing().getId()).isEqualTo(member2.getId());
        }
    }

    @Nested
    @DisplayName("회원 언팔로우")
    class UnfollowMember {

        @Test
        @DisplayName("회원 언팔로우 성공")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void unfollow_member_success() throws Exception {
            // given
            Long unfollowerId = member1.getId();
            Long unfollowingId = member2.getId();

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isNoContent())  // 204 No Content
                    .andDo(print());

            // 언팔로우 후 follow 관계가 삭제되었는지 확인
            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).isEmpty();  // 팔로우 관계가 삭제되었으므로 리스트는 비어 있어야 한다
        }

        @Test
        @DisplayName("회원 언팔로우 실패 - 팔로우 관계가 없는 회원을 언팔로우하려는 경우 예외 발생")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void unfollow_member_not_followed() throws Exception {
            // given
            Long unfollowerId = member2.getId();  // member2가 member1을 팔로우하지 않은 상태
            Long unfollowingId = member1.getId();

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isBadRequest())  // 400 Bad Request
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("이미 언팔로우한 회원입니다."))
                    .andDo(print());

            // 팔로우 관계가 여전히 존재하는지 확인
            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(1);  // 기존에 설정한 팔로우 관계는 삭제되지 않음
        }

        @Test
        @DisplayName("회원 언팔로우 실패 - 존재하지 않는 팔로워 또는 팔로잉을 언팔로우하려는 경우 예외 발생")
        @WithAuthMember(userId = "customUser", memberId = 1L)
        void unfollow_member_not_found() throws Exception {
            // given
            Long unfollowerId = 999L;  // 존재하지 않는 팔로워 ID
            Long unfollowingId = member2.getId() + 999L;

            // when & then
            mockMvc.perform(delete("/api/profile/follow/{memberId}", unfollowingId))
                    .andExpect(status().isNotFound())  // 404 Not Found
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 ID의 언팔로잉 회원을 찾을 수 없습니다."))
                    .andDo(print());

            // 팔로우 관계가 여전히 존재하는지 확인
            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(1);  // 기존에 설정한 팔로우 관계는 삭제되지 않음
        }
    }

    @Nested
    @DisplayName("팔로우 리스트 페이징")
    class FollowingPage {

        @Test
        @DisplayName("팔로우 리스트 페이징 성공")
        void get_followings_page_success() throws Exception {
            // given
            Long memberId = member1.getId();

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followings", memberId)
                            .param("page", "0")
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
            Long memberId = member1.getId() + 999L;  // 존재하지 않는 회원 ID

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followings", memberId)
                            .param("pageNumber", "1")
                            .param("pageSize", "10")
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
            Long memberId = member2.getId(); // member2의 팔로워 목록 조회

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followers", memberId)
                            .param("page", "0")
                            .param("size", "10")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andDo(print())
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1)) // member1이 팔로워로 존재
                    .andExpect(jsonPath("$.code").value(200));
        }

        @Test
        @DisplayName("팔로워 리스트 페이징 실패 - 해당 회원이 없는 경우")
        void get_followers_page_member_not_found() throws Exception {
            // given
            Long memberId = member2.getId() + 999L;  // 존재하지 않는 회원 ID

            // when & then
            mockMvc.perform(get("/api/profile/{memberId}/followers", memberId)
                            .param("page", "0")
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