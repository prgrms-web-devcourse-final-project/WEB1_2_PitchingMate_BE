package com.example.mate.domain.match.integration;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
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

import java.util.Arrays;
import java.util.List;

@SpringBootTest
@AutoConfigureMockMvc
@Transactional
class TeamRecordIntegrationTest {
    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private TeamRecordRepository teamRecordRepository;

    @BeforeEach
    void setUp() {
        teamRecordRepository.deleteAll();
    }

    @Test
    @DisplayName("팀 순위 조회 통합 테스트 - 성공")
    void getTeamRankings_Success() throws Exception {
        // given
        List<TeamRecord> teamRecords = Arrays.asList(
                createTeamRecord(TeamInfo.LG, 1, 86, 2, 56, 0.0),
                createTeamRecord(TeamInfo.KT, 2, 81, 2, 61, 4.5),
                createTeamRecord(TeamInfo.SSG, 3, 76, 2, 66, 8.5)
        );
        teamRecordRepository.saveAll(teamRecords);

        // when
        ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/teams/rankings")
                .accept(MediaType.APPLICATION_JSON));

        // then
        result.andExpect(MockMvcResultMatchers.status().isOk())
                .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(3))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].rank").value(1))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].teamName").value(TeamInfo.LG.fullName))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[0].wins").value(86))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[1].rank").value(2))
                .andExpect(MockMvcResultMatchers.jsonPath("$.data[2].rank").value(3));
    }

    private TeamRecord createTeamRecord(TeamInfo.Team team, int rank,
                                        int wins, int draws, int losses, double gamesBehind) {
        return TeamRecord.builder()
                .team(team)
                .rank(rank)
                .gamesPlayed(wins + draws + losses)
                .totalGames(144)
                .wins(wins)
                .draws(draws)
                .losses(losses)
                .gamesBehind(gamesBehind)
                .build();
    }
}