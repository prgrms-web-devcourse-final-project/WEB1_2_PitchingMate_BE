package com.example.mate.domain.match.integration;

import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.request.MatchRequest;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.repository.MatchRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;
import org.springframework.test.web.servlet.ResultActions;
import org.springframework.test.web.servlet.request.MockMvcRequestBuilders;
import org.springframework.test.web.servlet.result.MockMvcResultMatchers;
import org.springframework.transaction.annotation.Transactional;


import java.time.LocalDateTime;
import java.util.Arrays;


@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class MatchIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private MatchRepository matchRepository;

    @BeforeEach
    void setUp() {
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
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/matches/main")
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].homeTeam.id").value(TeamInfo.SSG.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].homeTeam.id").value(TeamInfo.LG.id));
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
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/matches/team/{teamId}", TeamInfo.LG.id)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].awayTeam.id").value(TeamInfo.NC.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].awayTeam.id").value(TeamInfo.KT.id));
    }

    @Test
    @DisplayName("팀별 경기 조회 - 실패 (존재하지 않는 팀)")
    void getTeamMatches_Fail_TeamNotFound() throws Exception {
        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/matches/team/{teamId}", 999L)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isNotFound())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("ERROR"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.message").value("팀을 찾을 수 없습니다"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.code").value(404));
    }

    @Test
    @DisplayName("팀별 완료된 경기 조회 - 성공")
    void getTeamCompletedMatches_Success() throws Exception {
        // given
        LocalDateTime pastTime1 = LocalDateTime.now().minusDays(1);
        LocalDateTime pastTime2 = LocalDateTime.now().minusDays(2);

        Match completedMatch1 = createCompletedMatch(TeamInfo.LG.id, TeamInfo.KT.id, StadiumInfo.JAMSIL.id, pastTime1, 5, 3);
        Match completedMatch2 = createCompletedMatch(TeamInfo.KIA.id, TeamInfo.LG.id, StadiumInfo.GWANGJU.id, pastTime2, 2, 7);
        Match scheduledMatch = createMatch(TeamInfo.LG.id, TeamInfo.NC.id, StadiumInfo.JAMSIL.id, LocalDateTime.now().plusDays(1));

        matchRepository.saveAll(Arrays.asList(completedMatch1, completedMatch2, scheduledMatch));

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/matches/team/{teamId}/completed", TeamInfo.LG.id)
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].status").value("COMPLETED"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].homeTeam.id").value(TeamInfo.LG.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].homeScore").value(5))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].awayTeam.id").value(TeamInfo.LG.id))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].awayScore").value(7));
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
}