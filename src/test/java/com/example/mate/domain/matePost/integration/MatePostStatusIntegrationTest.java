package com.example.mate.domain.matePost.integration;

import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.matePost.dto.request.MatePostCompleteRequest;
import com.example.mate.domain.matePost.dto.request.MatePostStatusRequest;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Status;
import com.example.mate.domain.matePost.entity.TransportType;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
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

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.example.mate.common.error.ErrorCode.*;
import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.hamcrest.Matchers.containsInAnyOrder;
import static org.hamcrest.Matchers.hasSize;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.patch;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
@WithAuthMember
public class MatePostStatusIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MatePostRepository matePostRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member authMember;
    private Member participant1;
    private Member participant2;
    private Match futureMatch;
    private Match pastMatch;
    private MatePost openPost;
    private MatePost completedPost;
    private MatePost closedPost;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        matePostRepository.deleteAll();
        matchRepository.deleteAll();
        memberRepository.deleteAll();

        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        // 테스트 멤버와 참여자들 생성
        authMember = createTestMember("testMember");
        participant1 = createTestMember("part1");
        participant2 = createTestMember("part2");

        // 테스트 매치 생성
        futureMatch = createMatch(LocalDateTime.now().plusDays(2));
        pastMatch = createMatch(LocalDateTime.now().minusDays(1));

        // 테스트 게시글 생성 - 직관 완료 테스트에 필요한 것만 생성
        openPost = createMatePost(futureMatch, 1L, Status.OPEN);
        completedPost = createMatePost(pastMatch, 2L, Status.VISIT_COMPLETE);
        closedPost = createMatePost(pastMatch, 1L, Status.CLOSED);
    }

    private Member createTestMember(String name) {
        return memberRepository.save(Member.builder()
                .name(name)
                .email(name + "@test.com")
                .nickname(name)
                .imageUrl(name + ".jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());
    }

    private Match createMatch(LocalDateTime matchTime) {
        return matchRepository.save(Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .status(SCHEDULED)
                .matchTime(matchTime)
                .build());
    }

    private MatePost createMatePost(Match match, Long teamId, Status status) {
        return matePostRepository.save(MatePost.builder()
                .author(authMember)
                .teamId(teamId)
                .match(match)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(status)
                .maxParticipants(3)
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transport(TransportType.PUBLIC)
                .build());
    }

    @Nested
    @DisplayName("메이트 게시글 상태 변경 테스트")
    class UpdateMatePostStatus {

        @Test
        @DisplayName("모집중에서 모집완료로 상태 변경 성공")
        void updateMatePostStatus_OpenToClosed_Success() throws Exception {
            // given
            List<Long> participantIds = Arrays.asList(participant1.getId(), participant2.getId());
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(openPost.getId()))
                    .andExpect(jsonPath("$.data.status").value("모집완료"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(print());

            // DB 검증
            MatePost updatedPost = matePostRepository.findById(openPost.getId()).orElseThrow();
            assertThat(updatedPost.getStatus()).isEqualTo(Status.CLOSED);
        }

        @Test
        @DisplayName("모집완료에서 모집중으로 상태 변경 성공")
        void updateMatePostStatus_ClosedToOpen_Success() throws Exception {
            // given
            List<Long> participantIds = Collections.singletonList(participant1.getId());  // 최소 1명의 참여자 필요
            MatePostStatusRequest request = new MatePostStatusRequest(Status.OPEN, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", closedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(closedPost.getId()))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(print());

            // DB 검증
            MatePost updatedPost = matePostRepository.findById(closedPost.getId()).orElseThrow();
            assertThat(updatedPost.getStatus()).isEqualTo(Status.OPEN);
        }

        @Test
        @DisplayName("직관완료 상태로 직접 변경 시도시 실패")
        void updateMatePostStatus_ToComplete_Failure() throws Exception {
            // given
            List<Long> participantIds = Arrays.asList(participant1.getId(), participant2.getId());
            MatePostStatusRequest request = new MatePostStatusRequest(Status.VISIT_COMPLETE, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(DIRECT_VISIT_COMPLETE_FORBIDDEN.getMessage()))
                    .andExpect(jsonPath("$.code").value(403))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(openPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.OPEN);
        }

        @Test
        @DisplayName("이미 직관완료된 게시글 상태 변경 시도시 실패")
        void updateMatePostStatus_AlreadyCompleted_Failure() throws Exception {
            // given
            List<Long> participantIds = Collections.singletonList(participant1.getId());  // 최소 1명의 참여자 필요
            MatePostStatusRequest request = new MatePostStatusRequest(Status.OPEN, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", completedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(ALREADY_COMPLETED_POST.getMessage()))
                    .andExpect(jsonPath("$.code").value(403))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(completedPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.VISIT_COMPLETE);
        }

        @Test
        @DisplayName("존재하지 않는 게시글의 상태 변경 시도시 실패")
        void updateMatePostStatus_PostNotFound_Failure() throws Exception {
            // given
            List<Long> participantIds = Arrays.asList(participant1.getId(), participant2.getId());
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", 999L)
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_NOT_FOUND_BY_ID.getMessage()))
                    .andExpect(jsonPath("$.code").value(404))
                    .andDo(print());
        }

        @Test
        @DisplayName("모집완료로 변경 시 참여자 수가 최대 인원을 초과하면 실패")
        void updateMatePostStatus_ExceedMaxParticipants_Failure() throws Exception {
            // given
            Member participant3 = createTestMember("part3");
            List<Long> participantIds = Arrays.asList(
                    participant1.getId(),
                    participant2.getId(),
                    participant3.getId()
            ); // 방장 포함 4명 (최대 인원 3명)
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_MAX_PARTICIPANTS_EXCEEDED.getMessage()))
                    .andExpect(jsonPath("$.code").value(400))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(openPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.OPEN);
        }

        @Test
        @DisplayName("모집완료로 변경 시 존재하지 않는 참여자 ID가 포함되면 실패")
        void updateMatePostStatus_InvalidParticipantId_Failure() throws Exception {
            // given
            List<Long> participantIds = Arrays.asList(participant1.getId(), 999L);
            MatePostStatusRequest request = new MatePostStatusRequest(Status.CLOSED, participantIds);

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/status", openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsString(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(INVALID_MATE_POST_PARTICIPANT_IDS.getMessage()))
                    .andExpect(jsonPath("$.code").value(400))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(openPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.OPEN);
        }
    }

    @Nested
    @DisplayName("직관 완료 처리")
    class completeVisit {

        @Test
        @DisplayName("직관 완료 처리 성공")
        void completeVisit_Success() throws Exception {
            // given
            MatePostCompleteRequest request = new MatePostCompleteRequest(
                    List.of(participant1.getId(), participant2.getId())
            );

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/complete",
                            closedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.id").value(closedPost.getId()))
                    .andExpect(jsonPath("$.data.status").value("직관완료"))
                    .andExpect(jsonPath("$.data.participantIds").isArray())
                    .andExpect(jsonPath("$.data.participantIds", hasSize(3)))
                    .andExpect(jsonPath("$.data.participantIds", containsInAnyOrder(
                            authMember.getId().intValue(),
                            participant1.getId().intValue(),
                            participant2.getId().intValue())))
                    .andDo(print());

            // DB 검증
            MatePost savedPost = matePostRepository.findById(closedPost.getId()).orElseThrow();
            assertThat(savedPost.getStatus()).isEqualTo(Status.VISIT_COMPLETE);
            assertThat(savedPost.getVisit()).isNotNull();
            assertThat(savedPost.getVisit().getParticipants()).hasSize(3);
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 권한 없음")
        @WithAuthMember(memberId = 2L)
        void completeVisit_Fail_NotAuthor() throws Exception {
            // given
            MatePostCompleteRequest request = new MatePostCompleteRequest(
                    List.of(participant1.getId(), participant2.getId())
            );

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/complete"
                            ,closedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_UPDATE_NOT_ALLOWED.getMessage()))
                    .andExpect(jsonPath("$.code").value(403))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(closedPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.CLOSED);
            assertThat(unchangedPost.getVisit()).isNull();
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 모집완료 상태가 아님")
        void completeVisit_Fail_NotClosedStatus() throws Exception {
            // given
            MatePost openPost = createMatePost(pastMatch, 1L, Status.OPEN);
            MatePostCompleteRequest request = new MatePostCompleteRequest(
                    List.of(participant1.getId(), participant2.getId())
            );

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/complete",
                            openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isBadRequest())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(NOT_CLOSED_STATUS_FOR_COMPLETION.getMessage()))
                    .andExpect(jsonPath("$.code").value(400))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(openPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.OPEN);
            assertThat(unchangedPost.getVisit()).isNull();
        }

        @Test
        @DisplayName("직관 완료 처리 실패 - 경기 시작 전")
        void completeVisit_Fail_BeforeMatchTime() throws Exception {
            // given
            MatePost futureClosedPost = createMatePost(futureMatch, 1L, Status.CLOSED);
            MatePostCompleteRequest request = new MatePostCompleteRequest(
                    List.of(participant1.getId(), participant2.getId())
            );

            // when & then
            mockMvc.perform(patch("/api/mates/{postId}/complete",
                            futureClosedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON)
                            .content(objectMapper.writeValueAsBytes(request)))
                    .andExpect(status().isForbidden())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_COMPLETE_TIME_NOT_ALLOWED.getMessage()))
                    .andExpect(jsonPath("$.code").value(403))
                    .andDo(print());

            // DB 검증
            MatePost unchangedPost = matePostRepository.findById(futureClosedPost.getId()).orElseThrow();
            assertThat(unchangedPost.getStatus()).isEqualTo(Status.CLOSED);
            assertThat(unchangedPost.getVisit()).isNull();
        }
    }

    @Test
    @DisplayName("직관 완료 처리 실패 - 존재하지 않는 참여자")
    void completeVisit_Fail_InvalidParticipant() throws Exception {
        // given
        MatePostCompleteRequest request = new MatePostCompleteRequest(
                List.of(participant1.getId(), 999L)
        );

        // when & then
        mockMvc.perform(patch("/api/mates/{postId}/complete",
                        closedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(INVALID_MATE_POST_PARTICIPANT_IDS.getMessage()))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());

        // DB 검증
        MatePost unchangedPost = matePostRepository.findById(closedPost.getId()).orElseThrow();
        assertThat(unchangedPost.getStatus()).isEqualTo(Status.CLOSED);
        assertThat(unchangedPost.getVisit()).isNull();
    }

    @Test
    @DisplayName("직관 완료 처리 실패 - 최대 참여 인원 초과")
    void completeVisit_Fail_ExceedMaxParticipants() throws Exception {
        // given
        Member participant3 = createTestMember("part3");
        MatePostCompleteRequest request = new MatePostCompleteRequest(
                List.of(participant1.getId(), participant2.getId(), participant3.getId())
        );

        // when & then
        mockMvc.perform(patch("/api/mates/{postId}/complete",
                        closedPost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsBytes(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(MATE_POST_MAX_PARTICIPANTS_EXCEEDED.getMessage()))
                .andExpect(jsonPath("$.code").value(400))
                .andDo(print());

        // DB 검증
        MatePost unchangedPost = matePostRepository.findById(closedPost.getId()).orElseThrow();
        assertThat(unchangedPost.getStatus()).isEqualTo(Status.CLOSED);
        assertThat(unchangedPost.getVisit()).isNull();
    }
}