package com.example.mate.domain.match.integration;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import java.util.Arrays;
import java.util.List;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("팀 순위 조회")
    class GetTeamRankings {
        @Test
        @DisplayName("전체 팀 순위 조회 성공")
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

        @Test
        @DisplayName("전체 팀 순위 조회 - 데이터 없음")
        void getTeamRankings_EmptyList() throws Exception {

            // when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/teams/rankings")
                    .accept(MediaType.APPLICATION_JSON));

            // then
            result.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data").isArray())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.length()").value(0));
        }
    }


    @Nested
    @DisplayName("특정 팀 순위 조회")
    class GetTeamRanking {
        @Test
        @DisplayName("특정 팀 순위 조회 성공")
        void getTeamRanking_Success() throws Exception {
            // given
            TeamRecord teamRecord = createTeamRecord(TeamInfo.LG, 1, 86, 2, 56, 0.0);
            teamRecordRepository.save(teamRecord);

            // when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/teams/rankings/" + TeamInfo.LG.id)
                    .accept(MediaType.APPLICATION_JSON));

            // then
            result.andExpect(MockMvcResultMatchers.status().isOk())
                    .andExpect(MockMvcResultMatchers.jsonPath("$.status").value("SUCCESS"))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.rank").value(1))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.teamName").value(TeamInfo.LG.fullName))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.wins").value(86))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.draws").value(2))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.losses").value(56))
                    .andExpect(MockMvcResultMatchers.jsonPath("$.data.gamesBehind").value(0.0));
        }

        @Test
        @DisplayName("존재하지 않는 팀 순위 조회 시 실패")
        void getTeamRanking_TeamNotFound() throws Exception {
            // given
            Long nonExistentTeamId = 999L;

            // when
            ResultActions result = mockMvc.perform(MockMvcRequestBuilders.get("/api/teams/rankings/" + nonExistentTeamId)
                    .accept(MediaType.APPLICATION_JSON));

            // then
            result.andExpect(MockMvcResultMatchers.status().isNotFound());
        }
    }

    private TeamRecord createTeamRecord(TeamInfo.Team team, int rank,
                                        int wins, int draws, int losses, double gamesBehind) {
        return TeamRecord.builder()
                .teamId(team.id)
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