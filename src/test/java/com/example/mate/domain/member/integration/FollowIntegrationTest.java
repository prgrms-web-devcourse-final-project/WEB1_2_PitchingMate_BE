package com.example.mate.domain.member.integration;

import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.FollowRepository;
import com.example.mate.domain.member.repository.MemberRepository;
import jakarta.transaction.Transactional;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.web.servlet.MockMvc;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
public class FollowIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private FollowRepository followRepository;

    @Autowired
    private MemberRepository memberRepository;

    private Member member1;
    private Member member2;
    private Follow follow;

    @BeforeEach
    void setUp() {
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
    @DisplayName("회원 팔로우 테스트")
    class FollowMember {

        @Test
        @DisplayName("다른 회원 팔로우 성공")
        void follow_member_success() throws Exception {
            // given
            Long followerId = member2.getId();
            Long followingId = member1.getId();

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
                    .andExpect(status().isOk())
                    .andDo(print());

            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(2); // 기존 1개 + 새로 1개 추가
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollower().getId()).isEqualTo(followerId);
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollowing().getId()).isEqualTo(followingId);
        }

        @Test
        @DisplayName("이미 팔로우한 회원을 다시 팔로우하려는 경우 예외 발생")
        void follow_member_already_followed() throws Exception {
            // given - 이미 팔로우 되어 있는 상태인 member1가 member2 팔로우 상황 가정
            Long followerId = member1.getId();
            Long followingId = member2.getId();

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
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
        @DisplayName("존재하지 않는 팔로워 또는 팔로잉을 팔로우하려는 경우 예외 발생")
        void follow_member_not_found() throws Exception {
            // given
            Long followerId = member1.getId() + 999L;
            Long followingId = member1.getId();

            // when & then
            mockMvc.perform(post("/api/profile/follow/{memberId}", followingId)
                            .param("followerId", String.valueOf(followerId)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value("해당 ID의 팔로워 회원을 찾을 수 없습니다."))
                    .andDo(print());

            List<Follow> savedFollows = followRepository.findAll();
            assertThat(savedFollows).size().isEqualTo(1);
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollower().getId()).isEqualTo(member1.getId());
            assertThat(savedFollows.get(savedFollows.size() - 1)
                    .getFollowing().getId()).isEqualTo(member2.getId());
        }
    }
}
