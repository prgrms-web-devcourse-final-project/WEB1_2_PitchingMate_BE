package com.example.mate.domain.match.integration;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.WeeklyMatchesResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.repository.MatchRepository;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.MvcResult;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.test.web.servlet.setup.MockMvcBuilders;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.filter.CharacterEncodingFilter;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.groups.Tuple.tuple;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private WebApplicationContext ctx;

    @Autowired
    private MatchRepository matchRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        this.mockMvc = MockMvcBuilders.webAppContextSetup(ctx)
                .addFilters(new CharacterEncodingFilter("UTF-8", true))
                .build();

        matchRepository.deleteAll();
    }

    @Test
    @DisplayName("메인 배너 경기 조회 - 성공 (이후 경기만 조회)")
    void getMainBannerMatches_Success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(1);
        LocalDateTime futureTime1 = now.plusDays(1);
        LocalDateTime futureTime2 = now.plusDays(2);

        Match pastMatch = createMatch(TeamInfo.KIA.id, TeamInfo.DOOSAN.id, StadiumInfo.GWANGJU.id, pastTime);
        Match futureMatch1 = createMatch(TeamInfo.LG.id, TeamInfo.KT.id, StadiumInfo.JAMSIL.id, futureTime1);
        Match futureMatch2 = createMatch(TeamInfo.SSG.id, TeamInfo.NC.id, StadiumInfo.INCHEON.id, futureTime2);
        matchRepository.saveAll(Arrays.asList(pastMatch, futureMatch1, futureMatch2));

        // when
        ResultActions result = mockMvc.perform(get("/api/matches/main")
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].homeTeam.id").value(TeamInfo.SSG.id))
                .andExpect(jsonPath("$.data[1].homeTeam.id").value(TeamInfo.LG.id));
    }

    @Test
    @DisplayName("팀별 경기 조회 - 성공 (이후 경기만 조회)")
    void getTeamMatches_Success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        LocalDateTime pastTime = now.minusDays(1);
        LocalDateTime futureTime1 = now.plusDays(1);
        LocalDateTime futureTime2 = now.plusDays(2);

        Match pastMatch = createMatch(TeamInfo.LG.id, TeamInfo.KIA.id, StadiumInfo.JAMSIL.id, pastTime);
        Match futureMatch1 = createMatch(TeamInfo.LG.id, TeamInfo.KT.id, StadiumInfo.JAMSIL.id, futureTime1);
        Match futureMatch2 = createMatch(TeamInfo.LG.id, TeamInfo.NC.id, StadiumInfo.JAMSIL.id, futureTime2);
        matchRepository.saveAll(Arrays.asList(pastMatch, futureMatch1, futureMatch2));

        // when
        ResultActions result = mockMvc.perform(get("/api/matches/team/{teamId}", TeamInfo.LG.id)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(2))
                .andExpect(jsonPath("$.data[0].awayTeam.id").value(TeamInfo.NC.id))
                .andExpect(jsonPath("$.data[1].awayTeam.id").value(TeamInfo.KT.id));
    }

    @Test
    @DisplayName("팀별 경기 조회 - 실패 (존재하지 않는 팀)")
    void getTeamMatches_Fail_TeamNotFound() throws Exception {
        // when
        ResultActions result = mockMvc.perform(get("/api/matches/team/{teamId}", 999L)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isNotFound())
                .andExpect(jsonPath("$.status").value("ERROR"))
                .andExpect(jsonPath("$.message").value("팀을 찾을 수 없습니다"))
                .andExpect(jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("팀별 완료된 경기 조회 - 성공")
    void getTeamCompletedMatches_Success() throws Exception {
        // given
        LocalDateTime now = LocalDateTime.now();
        List<Match> matches = new ArrayList<>();
        // 7개의 완료된 경기 생성
        for (int i = 1; i <= 7; i++) {
            matches.add(createCompletedMatch(
                    TeamInfo.LG.id,
                    TeamInfo.KT.id,
                    StadiumInfo.JAMSIL.id,
                    now.minusDays(i),
                    5 + i,
                    3 + i
            ));
        }
        matchRepository.saveAll(matches);

        // when
        ResultActions result = mockMvc.perform(get("/api/matches/team/{teamId}/completed", TeamInfo.LG.id)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data.length()").value(6))  // 최대 6개만 조회되는지 확인
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"));
    }

    private Match createCompletedMatch(Long homeTeamId, Long awayTeamId, Long stadiumId,
                                       LocalDateTime matchTime, Integer homeScore, Integer awayScore) {
        return Match.builder()
                .homeTeamId(homeTeamId)
                .awayTeamId(awayTeamId)
                .stadiumId(stadiumId)
                .matchTime(matchTime)
                .isCanceled(false)
                .status(MatchStatus.COMPLETED)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .build();
    }

    private Match createMatch(Long homeTeamId, Long awayTeamId, Long stadiumId, LocalDateTime matchTime) {
        return Match.builder()
                .homeTeamId(homeTeamId)
                .awayTeamId(awayTeamId)
                .stadiumId(stadiumId)
                .matchTime(matchTime)
                .isCanceled(false)
                .status(MatchStatus.SCHEDULED)
                .homeScore(null)
                .awayScore(null)
                .build();
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 - 정상 케이스")
    void getTeamWeeklyMatches_Success() throws Exception {
        // Given
        LocalDate startDate = LocalDate.of(2024, 3, 25); // 월요일
        Long teamId = TeamInfo.LG.id;

        saveTestMatches(teamId, startDate);

        // When
        ApiResponse<List<WeeklyMatchesResponse>> response =
                performWeeklyMatchesRequest(teamId, startDate);

        // Then
        assertWeeklyMatchesResponse(response, teamId, startDate);
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 - 존재하지 않는 팀")
    void getTeamWeeklyMatches_TeamNotFound() throws Exception {
        // Given
        Long invalidTeamId = 999L;
        LocalDate startDate = LocalDate.now();

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}/weekly", invalidTeamId)
                        .param("startDate", startDate.toString())
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isNotFound())
                .andExpect(jsonPath("$.message").value("팀을 찾을 수 없습니다"));
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 - 날짜 미입력시 현재 날짜 기준 조회")
    void getTeamWeeklyMatches_WithoutDate() throws Exception {
        // Given
        Long teamId = TeamInfo.LG.id;

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}/weekly", teamId)
                        .accept(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk());
    }

    private void saveTestMatches(Long teamId, LocalDate startDate) {
        List<Match> matches = Arrays.asList(
                createWeeklyMatch(teamId, TeamInfo.KT.id, startDate.plusDays(1)),  // 화요일
                createWeeklyMatch(teamId, TeamInfo.NC.id, startDate.plusDays(2)),  // 수요일
                createWeeklyMatch(teamId, TeamInfo.KIA.id, startDate.plusDays(8))  // 다음주 화요일
        );
        matchRepository.saveAll(matches);
    }

    private Match createWeeklyMatch(Long homeTeamId, Long awayTeamId, LocalDate matchDate) {
        return Match.builder()
                .homeTeamId(homeTeamId)
                .awayTeamId(awayTeamId)
                .stadiumId(StadiumInfo.JAMSIL.id)
                .matchTime(matchDate.atTime(18, 30))
                .isCanceled(false)
                .status(MatchStatus.SCHEDULED)
                .build();
    }

    private ApiResponse<List<WeeklyMatchesResponse>> performWeeklyMatchesRequest(
            Long teamId, LocalDate startDate) throws Exception {
        MvcResult mvcResult = mockMvc.perform(
                        get("/api/matches/team/{teamId}/weekly", teamId)
                                .param("startDate", startDate.toString())
                                .accept(MediaType.APPLICATION_JSON)
                                .characterEncoding("UTF-8"))
                .andExpect(status().isOk())
                .andReturn();

        return objectMapper.readValue(
                mvcResult.getResponse().getContentAsString(),
                new TypeReference<ApiResponse<List<WeeklyMatchesResponse>>>() {}
        );
    }

    private void assertWeeklyMatchesResponse(
            ApiResponse<List<WeeklyMatchesResponse>> response,
            Long teamId,
            LocalDate startDate
    ) {
        List<WeeklyMatchesResponse> weeklyResponses = response.getData();
        assertThat(weeklyResponses).hasSize(4);

        // 첫 주차 검증
        assertFirstWeek(weeklyResponses.get(0), teamId, startDate);
        // 두번째 주차 검증
        assertSecondWeek(weeklyResponses.get(1));
        // 나머지 주차 검증
        assertThat(weeklyResponses.get(2).getMatches()).isEmpty();
        assertThat(weeklyResponses.get(3).getMatches()).isEmpty();
    }

    private void assertFirstWeek(WeeklyMatchesResponse firstWeek, Long teamId, LocalDate startDate) {
        assertThat(firstWeek)
                .satisfies(week -> {
                    assertThat(week.getWeekNumber()).isEqualTo(1);
                    assertThat(week.getWeekLabel()).isEqualTo("3월 4주차");
                    assertThat(week.getMatches()).hasSize(2)
                            .extracting(
                                    match -> match.getMatchTime().toLocalDate(),
                                    match -> match.getHomeTeam().getId()
                            )
                            .containsExactly(
                                    tuple(startDate.plusDays(1), teamId),
                                    tuple(startDate.plusDays(2), teamId)
                            );
                });
    }

    private void assertSecondWeek(WeeklyMatchesResponse secondWeek) {
        assertThat(secondWeek.getMatches()).hasSize(1);
    }

    private void assertEmptyWeeks(List<WeeklyMatchesResponse> emptyWeeks) {
        emptyWeeks.forEach(week ->
                assertThat(week.getMatches()).isEmpty());
    }
}