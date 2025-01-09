package com.example.mate.domain.mateReview.integration;

import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.matePost.entity.*;
import com.example.mate.domain.matePost.repository.MatePostRepository;
import com.example.mate.domain.matePost.repository.VisitRepository;
import com.example.mate.domain.mateReview.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.mateReview.repository.MateReviewRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
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

import java.time.LocalDateTime;
import java.util.List;

import static com.example.mate.common.error.ErrorCode.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
class MateReviewIntegrationTest {

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
    private VisitRepository visitRepository;

    @Autowired
    private MateReviewRepository mateReviewRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    private Member author;
    private Member participant;
    private MatePost matePost;
    private Visit visit;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        mateReviewRepository.deleteAll();
        visitRepository.deleteAll();
        matePostRepository.deleteAll();
        matchRepository.deleteAll();
        memberRepository.deleteAll();

        // ID 시퀀스 초기화
        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        // 테스트 멤버 생성
        author = Member.builder()
                .name("작성자")
                .nickname("author")
                .email("author@test.com")
                .age(25)
                .gender(Gender.MALE)
                .imageUrl("default.jpg")
                .build();
        memberRepository.save(author); // ID: 1

        participant = Member.builder()
                .name("참여자")
                .nickname("participant")
                .email("participant@test.com")
                .age(25)
                .gender(Gender.MALE)
                .imageUrl("default.jpg")
                .build();
        memberRepository.save(participant); // ID: 2

        // 테스트용 Match 생성 및 저장
        Match match = Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .matchTime(LocalDateTime.now().plusDays(1))
                .status(MatchStatus.SCHEDULED)
                .isCanceled(false)
                .build();
        match = matchRepository.save(match);

        // 테스트용 MatePost 생성
        matePost = MatePost.builder()
                .author(author)
                .teamId(1L)
                .match(match)
                .title("같이 직관가요")
                .content("직관 메이트 구합니다")
                .status(Status.VISIT_COMPLETE)
                .maxParticipants(5)
                .age(Age.TWENTIES)
                .gender(Gender.ANY)
                .transport(TransportType.ANY)
                .build();
        matePostRepository.save(matePost);

        // Visit 생성 및 participant 추가
        matePost.complete(List.of(participant));
        visit = visitRepository.save(matePost.getVisit());
    }

    @Test
    @WithAuthMember(memberId = 2L) // participant의 ID로 설정
    @DisplayName("메이트 리뷰 정상 등록 - 참여자가 방장에게 리뷰")
    void createMateReview_Success() throws Exception {
        // given
        MateReviewCreateRequest request = new MateReviewCreateRequest(
                author.getId(),
                Rating.GOOD,
                "좋은 시간이었습니다!"
        );

        // when & then
        mockMvc.perform(post("/api/mates/review/{postId}", matePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.reviewerId").value(participant.getId()))
                .andExpect(jsonPath("$.data.revieweeId").value(author.getId()))
                .andExpect(jsonPath("$.data.revieweeNickName").value(author.getNickname()))
                .andExpect(jsonPath("$.data.content").value("좋은 시간이었습니다!"))
                .andExpect(jsonPath("$.data.rating").value(Rating.GOOD.getValue()));
    }

    @Test
    @WithAuthMember(memberId = 3L) // 참여하지 않은 사용자의 ID
    @DisplayName("메이트 리뷰 등록 실패 - 참여하지 않은 사용자가 리뷰 작성")
    void createMateReview_Fail_NotParticipant() throws Exception {
        // given
        Member nonParticipant = Member.builder()
                .name("비참여자")
                .nickname("nonParticipant")
                .email("non@test.com")
                .age(25)
                .gender(Gender.MALE)
                .imageUrl("default.jpg")
                .build();
        memberRepository.save(nonParticipant); // ID: 3

        MateReviewCreateRequest request = new MateReviewCreateRequest(
                author.getId(),
                Rating.GOOD,
                "좋은 시간이었습니다!"
        );

        // when & then
        mockMvc.perform(post("/api/mates/review/{postId}", matePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isForbidden())
                .andExpect(jsonPath("$.code").value(403))
                .andExpect(jsonPath("$.message").value(NOT_PARTICIPANT_OR_AUTHOR.getMessage()));
    }

    @Test
    @WithAuthMember(memberId = 2L)
    @DisplayName("메이트 리뷰 등록 실패 - 존재하지 않는 게시글")
    void createMateReview_Fail_PostNotFound() throws Exception {
        // given
        MateReviewCreateRequest request = new MateReviewCreateRequest(
                author.getId(),
                Rating.GOOD,
                "좋은 시간이었습니다!"
        );

        // when & then
        mockMvc.perform(post("/api/mates/review/{postId}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(MATE_POST_NOT_FOUND_BY_ID.getMessage()));
    }

    @Test
    @WithAuthMember(memberId = 2L)
    @DisplayName("메이트 리뷰 등록 실패 - 존재하지 않는 리뷰 대상자")
    void createMateReview_Fail_RevieweeNotFound() throws Exception {
        // given
        MateReviewCreateRequest request = new MateReviewCreateRequest(
                999L,
                Rating.GOOD,
                "좋은 시간이었습니다!"
        );

        // when & then
        mockMvc.perform(post("/api/mates/review/{postId}", matePost.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andDo(print())
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value(MEMBER_NOT_FOUND_BY_ID.getMessage()));
    }
}