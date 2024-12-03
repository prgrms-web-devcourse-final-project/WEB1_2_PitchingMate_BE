package com.example.mate.domain.match.controller;


import static org.hamcrest.Matchers.hasSize;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.dto.response.WeeklyMatchesResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.service.MatchService;
import com.example.mate.domain.match.util.WeekCalculator;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;


@WebMvcTest(MatchController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Test
    @DisplayName("메인 배너 경기 조회 API 테스트")
    void getMainBannerMatches() throws Exception {
        // Given
        List<MatchResponse> mockResponses = List.of(
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.KIA.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.GWANGJU.id)
                                .matchTime(LocalDateTime.now().plusDays(1))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.NC.id)
                                .awayTeamId(TeamInfo.SSG.id)
                                .stadiumId(StadiumInfo.CHANGWON.id)
                                .matchTime(LocalDateTime.now().plusDays(2))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.DOOSAN.id)
                                .awayTeamId(TeamInfo.KT.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().plusDays(3))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.SAMSUNG.id)
                                .awayTeamId(TeamInfo.LOTTE.id)
                                .stadiumId(StadiumInfo.DAEGU.id)
                                .matchTime(LocalDateTime.now().plusDays(4))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.HANWHA.id)
                                .awayTeamId(TeamInfo.KIWOOM.id)
                                .stadiumId(StadiumInfo.DAEJEON.id)
                                .matchTime(LocalDateTime.now().plusDays(5))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null)
        );
        when(matchService.getMainBannerMatches()).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/matches/main")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(5)));
    }

    @Test
    @DisplayName("팀별 경기 조회 API 테스트")
    void getTeamMatches() throws Exception {
        // Given
        Long teamId = 1L;
        List<MatchResponse> mockResponses = List.of(
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.KIA.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.GWANGJU.id)
                                .matchTime(LocalDateTime.now().plusDays(1))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.NC.id)
                                .awayTeamId(TeamInfo.SSG.id)
                                .stadiumId(StadiumInfo.CHANGWON.id)
                                .matchTime(LocalDateTime.now().plusDays(2))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.DOOSAN.id)
                                .awayTeamId(TeamInfo.KT.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().plusDays(3))
                                .isCanceled(false)
                                .status(MatchStatus.SCHEDULED)
                                .build(),
                        null)
        );
        when(matchService.getTeamMatches(teamId)).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data.length()").value(3));
    }

    @Test
    @DisplayName("팀별 완료된 경기 조회 API 테스트")
    void getTeamCompletedMatches() throws Exception {
        // Given
        Long teamId = TeamInfo.LG.id;
        List<MatchResponse> mockResponses = Arrays.asList(
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.LG.id)
                                .awayTeamId(TeamInfo.KT.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().minusDays(1))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(5)
                                .awayScore(3)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.DOOSAN.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().minusDays(2))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(2)
                                .awayScore(6)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.LG.id)
                                .awayTeamId(TeamInfo.SAMSUNG.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().minusDays(3))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(8)
                                .awayScore(4)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.KIWOOM.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.GOCHEOK.id)
                                .matchTime(LocalDateTime.now().minusDays(4))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(1)
                                .awayScore(7)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.LG.id)
                                .awayTeamId(TeamInfo.NC.id)
                                .stadiumId(StadiumInfo.JAMSIL.id)
                                .matchTime(LocalDateTime.now().minusDays(5))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(3)
                                .awayScore(3)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.HANWHA.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.DAEJEON.id)
                                .matchTime(LocalDateTime.now().minusDays(6))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(4)
                                .awayScore(9)
                                .isCanceled(false)
                                .build(),
                        TeamInfo.LG.id)
        );
        when(matchService.getTeamCompletedMatches(teamId)).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}/completed", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(mockResponses.size())));  // 동적으로 크기 체크
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 API 테스트")
    void getTeamWeeklyMatches() throws Exception {
        // Given
        Long teamId = TeamInfo.LG.id;
        LocalDate startDate = LocalDate.of(2024, 3, 25); // 월요일

        List<WeeklyMatchesResponse> mockResponses = List.of(
                createWeeklyMatchResponse(1, startDate, 2),            // 1주차 2경기
                createWeeklyMatchResponse(2, startDate.plusWeeks(1), 1), // 2주차 1경기
                createWeeklyMatchResponse(3, startDate.plusWeeks(2), 2), // 3주차 2경기
                createWeeklyMatchResponse(4, startDate.plusWeeks(3), 1)  // 4주차 1경기
        );

        when(matchService.getTeamWeeklyMatches(eq(teamId), any(LocalDate.class)))
                .thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}/weekly", teamId)
                        .param("startDate", "2024-03-25")
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data", hasSize(4)))
                .andExpect(jsonPath("$.data[0].weekNumber").value(1))
                .andExpect(jsonPath("$.data[0].weekLabel").value("3월 4주차"))
                .andExpect(jsonPath("$.data[0].matches", hasSize(2)));
    }

    private Match createMatch(Long teamId, LocalDateTime matchTime) {
        return Match.builder()
                .homeTeamId(teamId)
                .awayTeamId(TeamInfo.KT.id)
                .stadiumId(StadiumInfo.JAMSIL.id)
                .matchTime(matchTime)
                .isCanceled(false)
                .status(MatchStatus.SCHEDULED)
                .build();
    }

    private WeeklyMatchesResponse createWeeklyMatchResponse(int weekNumber, LocalDate weekStart, int matchCount) {
        List<MatchResponse> matches = new ArrayList<>();
        for (int i = 0; i < matchCount; i++) {
            Match match = createMatch(
                    TeamInfo.LG.id,
                    weekStart.plusDays(i).atTime(18, 30)
            );
            matches.add(MatchResponse.from(match, TeamInfo.LG.id));
        }

        return WeeklyMatchesResponse.builder()
                .weekNumber(weekNumber)
                .weekLabel(WeekCalculator.generateWeekLabel(weekStart))
                .weekStartDate(weekStart)
                .weekEndDate(weekStart.plusDays(6))
                .matches(matches)
                .build();
    }
}