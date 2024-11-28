package com.example.mate.domain.match.controller;


import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;

import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.service.MatchService;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.autoconfigure.web.servlet.WebMvcTest;
import org.springframework.boot.test.mock.mockito.MockBean;

import org.springframework.data.jpa.mapping.JpaMetamodelMappingContext;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;


@WebMvcTest(MatchController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class MatchControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private MatchService matchService;

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
                        teamId),
                MatchResponse.from(Match.builder()
                                .homeTeamId(TeamInfo.KIA.id)
                                .awayTeamId(TeamInfo.LG.id)
                                .stadiumId(StadiumInfo.GWANGJU.id)
                                .matchTime(LocalDateTime.now().minusDays(2))
                                .status(MatchStatus.COMPLETED)
                                .homeScore(2)
                                .awayScore(7)
                                .isCanceled(false)
                                .build(),
                        teamId)
        );
        when(matchService.getTeamCompletedMatches(teamId)).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/matches/team/{teamId}/completed", teamId)
                        .contentType(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(2)))
                .andExpect(jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(jsonPath("$.data[0].homeScore").value(5))
                .andExpect(jsonPath("$.data[1].awayScore").value(7));
    }
}