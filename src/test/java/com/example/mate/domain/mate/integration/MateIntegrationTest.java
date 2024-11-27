package com.example.mate.domain.mate.integration;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.mate.dto.request.MatePostCreateRequest;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import com.example.mate.domain.mate.repository.MateRepository;
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
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

import static com.example.mate.domain.match.entity.MatchStatus.SCHEDULED;
import static org.assertj.core.api.Assertions.assertThat;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
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

    private Member testMember;
    private Match testMatch;

    @BeforeEach
    void setUp() {
        mateRepository.deleteAll();
        matchRepository.deleteAll();
        memberRepository.deleteAll();

        testMember = memberRepository.save(Member.builder()
                .name("테스트유저")
                .email("test@test.com")
                .nickname("테스트계정")
                .imageUrl("test.jpg")
                .gender(Gender.FEMALE)
                .age(25)
                .manner(0.3f)
                .build());

        testMatch = matchRepository.save(Match.builder()
                .homeTeamId(1L)
                .awayTeamId(2L)
                .stadiumId(1L)
                .status(SCHEDULED)
                .matchTime(LocalDateTime.now().plusDays(1))
                .build());
    }

    private void assertMatePostEquals(MatePost actual, MatePostCreateRequest expected) {
        assertThat(actual.getAuthor()).isEqualTo(testMember);
        assertThat(actual.getTeamId()).isEqualTo(expected.getTeamId());
        assertThat(actual.getMatch()).isEqualTo(testMatch);
        assertThat(actual.getTitle()).isEqualTo(expected.getTitle());
        assertThat(actual.getContent()).isEqualTo(expected.getContent());
        assertThat(actual.getStatus()).isEqualTo(Status.OPEN);
        assertThat(actual.getMaxParticipants()).isEqualTo(expected.getMaxParticipants());
        assertThat(actual.getAge()).isEqualTo(expected.getAge());
        assertThat(actual.getGender()).isEqualTo(expected.getGender());
        assertThat(actual.getTransport()).isEqualTo(expected.getTransportType());
    }

    private void performErrorTest(MockMultipartFile data, String errorCode, int expectedStatus) throws Exception {
        mockMvc.perform(multipart("/api/mates")
                        .file(data))
                .andExpect(status().is(expectedStatus))
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.code").value(expectedStatus))
                .andExpect(jsonPath("$.message").exists())
                .andDo(print());

        assertThat(mateRepository.findAll()).isEmpty();
    }

    @Test
    @DisplayName("메이트 게시글 작성 성공")
    void createMatePost_Success() throws Exception {
        // given
        MatePostCreateRequest request = MatePostCreateRequest.builder()
                .memberId(testMember.getId())
                .teamId(1L)
                .matchId(testMatch.getId())
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
                .andExpect(jsonPath("$.data.status").value("OPEN"))
                .andExpect(jsonPath("$.code").value(200))
                .andDo(print());

        // DB 검증
        List<MatePost> savedPosts = mateRepository.findAll();
        assertThat(savedPosts).hasSize(1);
        assertMatePostEquals(savedPosts.get(0), request);
    }


    @Test
    @DisplayName("존재하지 않는 회원으로 메이트 게시글 작성 시 실패")
    void createMatePost_WithInvalidMember() throws Exception {
        // given
        MatePostCreateRequest request = MatePostCreateRequest.builder()
                .memberId(999L) // 존재하지 않는 회원 ID
                .teamId(1L)
                .matchId(testMatch.getId())
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
        performErrorTest(data, "MEMBER_NOT_FOUND_BY_ID", 404);
    }

    @Test
    @DisplayName("존재하지 않는 경기로 메이트 게시글 작성 시 실패")
    void createMatePost_WithInvalidMatch() throws Exception {
        // given
        MatePostCreateRequest request = MatePostCreateRequest.builder()
                .memberId(testMember.getId())
                .teamId(1L)
                .matchId(999L) // 존재하지 않는 경기 ID
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
        performErrorTest(data, "MATCH_NOT_FOUND_BY_ID", 404);
    }

    @Test
    @DisplayName("잘못된 요청 데이터로 메이트 게시글 작성 시 실패")
    void createMatePost_WithInvalidRequest() throws Exception {
        // given
        MatePostCreateRequest request = MatePostCreateRequest.builder()
                .memberId(testMember.getId())
                .teamId(1L)
                .matchId(testMatch.getId())
                .title("") // 빈 제목
                .content("통합 테스트 내용")
                .age(Age.TWENTIES)
                .maxParticipants(11) // 최대 인원 초과
                .gender(null)
                .transportType(TransportType.PUBLIC)
                .build();

        MockMultipartFile data = new MockMultipartFile(
                "data",
                "",
                MediaType.APPLICATION_JSON_VALUE,
                objectMapper.writeValueAsBytes(request)
        );

        // when & then
        performErrorTest(data, "INVALID_REQUEST", 500);
    }
}