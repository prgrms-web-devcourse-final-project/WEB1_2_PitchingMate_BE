package com.example.mate.domain.mate.integration;

import static com.example.mate.common.error.ErrorCode.MATCH_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.MATE_POST_NOT_FOUND_BY_ID;
import static com.example.mate.common.error.ErrorCode.TEAM_NOT_FOUND;
import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.delete;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.config.WithAuthMember;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.dto.request.MatePostUpdateRequest;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.repository.MateRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import java.time.LocalDateTime;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.domain.PageRequest;
import org.springframework.http.MediaType;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

@SpringBootTest
@AutoConfigureMockMvc(addFilters = false)
@Transactional
//@WithMockUser(username = "mock-user", roles = "USER")
public class MateIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private MemberRepository memberRepository;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private MateRepository mateRepository;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    // 테스트에서 공통으로 사용할 객체들
    private Member testMember;
    private Match futureMatch;
    private Match pastMatch;
    private MatePost openPost;
    private MatePost closedPost;
    private MatePost completedPost;

    @BeforeEach
    void setUp() {
        // 기존 데이터 정리
        mateRepository.deleteAll();
        matchRepository.deleteAll();
        memberRepository.deleteAll();

        jdbcTemplate.execute("ALTER TABLE member ALTER COLUMN id RESTART WITH 1");

        // 테스트 멤버 생성
        testMember = createTestMember();

        // 테스트 매치 생성
        futureMatch = createMatch(LocalDateTime.now().plusDays(2));
        pastMatch = createMatch(LocalDateTime.now().minusDays(1));

        // 테스트 게시글 생성
        openPost = createMatePost(futureMatch, 1L, Status.OPEN);
        closedPost = createMatePost(futureMatch, 1L, Status.CLOSED);
        completedPost = createMatePost(pastMatch, 2L, Status.VISIT_COMPLETE);
    }

    // 테스트 데이터 생성 헬퍼 메소드들
    private Member createTestMember() {
        return memberRepository.save(Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .nickname("테스트계정")
                .imageUrl("test.jpg")
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
        return mateRepository.save(MatePost.builder()
                .author(testMember)
                .teamId(teamId)
                .match(match)
                .title("테스트 제목")
                .content("테스트 내용")
                .status(status)
                .maxParticipants(4)
                .imageUrl("imageUrl")
                .age(Age.TWENTIES)
                .gender(Gender.FEMALE)
                .transport(TransportType.PUBLIC)
                .build());
    }

    private void performErrorTest(MockMultipartFile data, String errorMessage, int expectedStatus) throws Exception {
        mateRepository.deleteAll();

        mockMvc.perform(multipart("/api/mates")
                        .file(data))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value(errorMessage))
                .andExpect(jsonPath("$.data").isEmpty())
                .andExpect(jsonPath("$.code").value(expectedStatus))
                .andDo(print());

        assertThat(mateRepository.findAll()).isEmpty();
    }

    @Nested
    @DisplayName("메이트 게시글 작성")
    class CreateMatePost {

        @Test
        @DisplayName("메이트 게시글 작성 성공")
        @WithAuthMember
        void createMatePost_Success() throws Exception {
            // given
            MatePostCreateRequest request = MatePostCreateRequest.builder()
                    .teamId(1L)
                    .matchId(futureMatch.getId())
                    .title("통합 테스트 제목")
                    .content("통합 테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            // when & then
            mockMvc.perform(multipart("/api/mates")
                            .file(data))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andDo(print());

            // DB에 저장된 값 검증
            List<MatePost> savedPosts = mateRepository.findAll();
            MatePost savedPost = savedPosts.get(savedPosts.size() - 1);

            assertThat(savedPosts).hasSize(4);
            assertThat(savedPost.getAuthor().getId()).isEqualTo(testMember.getId());
            assertThat(savedPost.getTeamId()).isEqualTo(request.getTeamId());
            assertThat(savedPost.getMatch().getId()).isEqualTo(request.getMatchId());
            assertThat(savedPost.getTitle()).isEqualTo(request.getTitle());
            assertThat(savedPost.getContent()).isEqualTo(request.getContent());
            assertThat(savedPost.getStatus()).isEqualTo(Status.OPEN);
            assertThat(savedPost.getMaxParticipants()).isEqualTo(request.getMaxParticipants());
            assertThat(savedPost.getAge()).isEqualTo(request.getAge());
            assertThat(savedPost.getGender()).isEqualTo(request.getGender());
            assertThat(savedPost.getTransport()).isEqualTo(request.getTransportType());
            assertThat(savedPost.getAuthor().getManner()).isCloseTo(0.301f, within(0.0001f));
        }

        @Test
        @DisplayName("존재하지 않는 경기로 메이트 게시글 작성 시 실패")
        @WithAuthMember
        void createMatePost_WithInvalidMatch() throws Exception {
            MatePostCreateRequest request = MatePostCreateRequest.builder()
                    .teamId(1L)
                    .matchId(999L)
                    .title("통합 테스트 제목")
                    .content("통합 테스트 내용")
                    .age(Age.TWENTIES)
                    .maxParticipants(4)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();

            MockMultipartFile data = new MockMultipartFile(
                    "data",
                    "",
                    MediaType.APPLICATION_JSON_VALUE,
                    objectMapper.writeValueAsBytes(request)
            );

            performErrorTest(data, MATCH_NOT_FOUND_BY_ID.getMessage(), 404);
        }
    }

    @Nested
    @DisplayName("메인 페이지 메이트 게시글 조회")
    class GetMainPageMatePosts {

        @Test
        @DisplayName("메인 페이지 메이트 게시글 목록 조회 성공 - 팀 ID 있음")
        void getMatePostsMain_WithTeamId_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates/main")
                            .param("teamId", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    // 첫 번째 게시글 (OPEN) 검증
                    .andExpect(jsonPath("$.data[0].title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data[0].status").value("모집중"))
                    .andExpect(jsonPath("$.data[0].maxParticipants").value(4))
                    .andExpect(jsonPath("$.data[0].age").value("20대"))
                    .andExpect(jsonPath("$.data[0].gender").value("여자"))
                    .andExpect(jsonPath("$.data[0].transportType").value("대중교통"))
                    .andExpect(jsonPath("$.data[0].rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data[0].location").value("광주-기아 챔피언스 필드"))
                    // 두 번째 게시글 (CLOSED) 검증
                    .andExpect(jsonPath("$.data[1].title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data[1].status").value("모집완료"))
                    .andExpect(jsonPath("$.data[1].maxParticipants").value(4))
                    .andExpect(jsonPath("$.data[1].age").value("20대"))
                    .andExpect(jsonPath("$.data[1].gender").value("여자"))
                    .andExpect(jsonPath("$.data[1].transportType").value("대중교통"))
                    .andExpect(jsonPath("$.data[1].rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data[1].location").value("광주-기아 챔피언스 필드"))
                    .andDo(print());
        }

        @Test
        @DisplayName("메인 페이지 메이트 게시글 목록 조회 성공 - 팀 ID 없음")
        void getMatePostsMain_WithoutTeamId_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates/main")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    // 첫 번째 게시글 (OPEN) 검증
                    .andExpect(jsonPath("$.data[0].title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data[0].status").value("모집중"))
                    .andExpect(jsonPath("$.data[0].maxParticipants").value(4))
                    .andExpect(jsonPath("$.data[0].age").value("20대"))
                    .andExpect(jsonPath("$.data[0].gender").value("여자"))
                    .andExpect(jsonPath("$.data[0].transportType").value("대중교통"))
                    .andExpect(jsonPath("$.data[0].rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data[0].location").value("광주-기아 챔피언스 필드"))
                    // 두 번째 게시글 (CLOSED) 검증
                    .andExpect(jsonPath("$.data[1].title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data[1].status").value("모집완료"))
                    .andExpect(jsonPath("$.data[1].maxParticipants").value(4))
                    .andExpect(jsonPath("$.data[1].age").value("20대"))
                    .andExpect(jsonPath("$.data[1].gender").value("여자"))
                    .andExpect(jsonPath("$.data[1].transportType").value("대중교통"))
                    .andExpect(jsonPath("$.data[1].rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data[1].location").value("광주-기아 챔피언스 필드"))
                    .andDo(print());
        }

        @Test
        @DisplayName("과거 경기 또는 완료된 게시글은 조회되지 않음")
        void getMatePostsMain_ExcludePastAndCompleted() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates/main")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.data").isArray())
                    .andExpect(jsonPath("$.data.length()").value(2))
                    .andExpect(jsonPath("$.data[0].status").value("모집중"))
                    .andExpect(jsonPath("$.data[1].status").value("모집완료"))
                    .andDo(print());

            // DB 검증
            List<MatePost> posts = mateRepository.findMainPagePosts(
                    null,
                    LocalDateTime.now(),
                    List.of(Status.OPEN, Status.CLOSED),
                    PageRequest.of(0, 3)
            );

            assertThat(posts).hasSize(2)
                    .doesNotContain(completedPost)
                    .extracting(MatePost::getMatch)
                    .doesNotContain(pastMatch);
        }
    }


    @Nested
    @DisplayName("메이트 페이지 게시글 조회")
    class GetMatePagePosts {
        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 필터 없음")
        void getMatePagePosts_NoFilter_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.totalElements").value(2))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 팀 필터")
        void getMatePagePosts_TeamFilter_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("teamId", "1")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(2))
                    .andExpect(jsonPath("$.data.content[0].rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data.content[1].rivalTeamName").value("LG"))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 연령대 필터")
        void getMatePagePosts_AgeFilter_Success() throws Exception {
            // given
            MatePost thirtyPost = createMatePost(futureMatch, 1L, Status.OPEN);
            MatePostUpdateRequest updateRequest = MatePostUpdateRequest.builder()
                    .teamId(1L)
                    .matchId(futureMatch.getId())
                    .title("30대 게시글")
                    .content("내용")
                    .maxParticipants(4)
                    .age(Age.THIRTIES)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();
            thirtyPost.updatePost(updateRequest, futureMatch, null);
            mateRepository.save(thirtyPost);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("age", "30대")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].age").value("30대"))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 성별 필터")
        void getMatePagePosts_GenderFilter_Success() throws Exception {
            // given
            MatePost malePost = createMatePost(futureMatch, 1L, Status.OPEN);
            MatePostUpdateRequest updateRequest = MatePostUpdateRequest.builder()
                    .teamId(1L)
                    .matchId(futureMatch.getId())
                    .title("남자 게시글")
                    .content("내용")
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.MALE)
                    .transportType(TransportType.PUBLIC)
                    .build();
            malePost.updatePost(updateRequest, futureMatch, null);
            mateRepository.save(malePost);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("gender", "남자")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].gender").value("남자"))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 최대 인원 필터")
        void getMatePagePosts_MaxParticipantsFilter_Success() throws Exception {
            // given
            MatePost sixPeoplePost = createMatePost(futureMatch, 1L, Status.OPEN);
            MatePostUpdateRequest updateRequest = MatePostUpdateRequest.builder()
                    .teamId(1L)
                    .matchId(futureMatch.getId())
                    .title("6인 모집 게시글")
                    .content("내용")
                    .maxParticipants(6)
                    .age(Age.TWENTIES)
                    .gender(Gender.FEMALE)
                    .transportType(TransportType.PUBLIC)
                    .build();
            sixPeoplePost.updatePost(updateRequest, futureMatch, null);
            mateRepository.save(sixPeoplePost);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("maxParticipants", "6")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(1))
                    .andExpect(jsonPath("$.data.content[0].maxParticipants").value(6))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 성공 - 이동수단 필터")
        void getMatePagePosts_TransportFilter_Success() throws Exception {
            // given
            MatePost carpoolPost = createMatePost(futureMatch, 1L, Status.OPEN);
            MatePostUpdateRequest updateRequest = MatePostUpdateRequest.builder()
                    .teamId(1L)
                    .matchId(futureMatch.getId())
                    .title("대중교통 게시글")
                    .content("내용")
                    .maxParticipants(4)
                    .age(Age.TWENTIES)
                    .gender(Gender.MALE)
                    .transportType(TransportType.PUBLIC)
                    .build();
            carpoolPost.updatePost(updateRequest, futureMatch, null);
            mateRepository.save(carpoolPost);

            // when & then
            mockMvc.perform(get("/api/mates")
                            .param("transportType", "대중교통")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.content").isArray())
                    .andExpect(jsonPath("$.data.content.length()").value(3))
                    .andExpect(jsonPath("$.data.content[0].transportType").value("대중교통"))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 페이지 게시글 목록 조회 실패 - 존재하지 않는 팀")
        void getMatePagePosts_InvalidTeamId_Failure() throws Exception {
            mockMvc.perform(get("/api/mates")
                            .param("teamId", "999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(TEAM_NOT_FOUND.getMessage()))
                    .andExpect(jsonPath("$.code").value(404))
                    .andDo(print());
        }
    }

    @Nested
    @DisplayName("메이트 게시글 상세 조회")
    class GetMatePostDetail {

        @Test
        @DisplayName("메이트 게시글 상세 조회 성공")
        void getMatePostDetail_Success() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates/" + openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.status").value("SUCCESS"))
                    .andExpect(jsonPath("$.code").value(200))
                    .andExpect(jsonPath("$.data.postId").value(openPost.getId()))
                    .andExpect(jsonPath("$.data.authorId").value(testMember.getId()))
                    .andExpect(jsonPath("$.data.matchId").value(futureMatch.getId()))
                    .andExpect(jsonPath("$.data.title").value("테스트 제목"))
                    .andExpect(jsonPath("$.data.content").value("테스트 내용"))
                    .andExpect(jsonPath("$.data.status").value("모집중"))
                    .andExpect(jsonPath("$.data.maxParticipants").value(4))
                    .andExpect(jsonPath("$.data.age").value("20대"))
                    .andExpect(jsonPath("$.data.gender").value("여자"))
                    .andExpect(jsonPath("$.data.transportType").value("대중교통"))
                    .andExpect(jsonPath("$.data.rivalTeamName").value("LG"))
                    .andExpect(jsonPath("$.data.location").value("광주-기아 챔피언스 필드"))
                    .andExpect(jsonPath("$.data.userImageUrl").value(FileUtils.getThumbnailImageUrl("test.jpg")))
                    .andExpect(jsonPath("$.data.nickname").value("테스트계정"))
                    .andExpect(jsonPath("$.data.manner").value(0.3))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 실패 - 존재하지 않는 게시글")
        void getMatePostDetail_NotFound() throws Exception {
            // when & then
            mockMvc.perform(get("/api/mates/999")
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_NOT_FOUND_BY_ID.getMessage()))
                    .andExpect(jsonPath("$.code").value(404))
                    .andDo(print());
        }

        @Test
        @DisplayName("메이트 게시글 상세 조회 - 모든 상태의 게시글 조회 가능")
        void getMatePostDetail_AllStatusAccessible() throws Exception {
            // Test OPEN status
            mockMvc.perform(get("/api/mates/" + openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("모집중"));

            // Test CLOSED status
            mockMvc.perform(get("/api/mates/" + closedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("모집완료"));

            // Test COMPLETE status
            mockMvc.perform(get("/api/mates/" + completedPost.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isOk())
                    .andExpect(jsonPath("$.data.status").value("직관완료"));
        }
    }

    @Nested
    @DisplayName("메이트 게시글 삭제")
    class DeleteMatePost {

        @Test
        @DisplayName("메이트 게시글 삭제 성공")
        @WithAuthMember
        void deleteMatePost_Success() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/mates/{postId}", openPost.getId())
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNoContent())
                    .andDo(print());

            // DB 검증
            assertThat(mateRepository.findById(openPost.getId())).isEmpty();
            assertThat(openPost.getAuthor().getManner()).isCloseTo(0.299f, within(0.0001f));
        }

        @Test
        @DisplayName("메이트 게시글 삭제 실패 - 존재하지 않는 게시글")
        @WithAuthMember
        void deleteMatePost_NotFound() throws Exception {
            // when & then
            mockMvc.perform(delete("/api/mates/{postId}", 999L)
                            .contentType(MediaType.APPLICATION_JSON))
                    .andExpect(status().isNotFound())
                    .andExpect(jsonPath("$.status").value("ERROR"))
                    .andExpect(jsonPath("$.message").value(MATE_POST_NOT_FOUND_BY_ID.getMessage()))
                    .andExpect(jsonPath("$.code").value(404))
                    .andDo(print());

            // DB 검증 - 기존 게시글들은 여전히 존재
            assertThat(mateRepository.findAll()).hasSize(3);
        }
    }
}