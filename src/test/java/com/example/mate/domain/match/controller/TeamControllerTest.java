package com.example.mate.domain.match.controller;

import static org.hamcrest.Matchers.hasSize;
import static org.mockito.Mockito.when;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultHandlers.print;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

import com.example.mate.common.security.filter.JwtCheckFilter;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.service.TeamService;
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

@WebMvcTest(TeamController.class)
@MockBean(JpaMetamodelMappingContext.class)
@AutoConfigureMockMvc(addFilters = false)
class TeamControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private TeamService teamService;

    @MockBean
    private JwtCheckFilter jwtCheckFilter;

    @Test
    @DisplayName("팀 순위 조회 API 테스트")
    void getTeamRankings() throws Exception {
        // Given
        List<TeamResponse.Detail> mockResponses = List.of(
                createTeamResponse(TeamInfo.LG, 1, 86, 2, 56, 0.0),
                createTeamResponse(TeamInfo.KT, 2, 81, 2, 61, 4.5),
                createTeamResponse(TeamInfo.SSG, 3, 76, 2, 66, 8.5)
        );

        when(teamService.getTeamRankings()).thenReturn(mockResponses);

        // When & Then
        mockMvc.perform(get("/api/teams/rankings")
                        .accept(MediaType.APPLICATION_JSON))
                .andDo(print())
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.status").value("SUCCESS"))
                .andExpect(jsonPath("$.data").isArray())
                .andExpect(jsonPath("$.data", hasSize(3)))
                .andExpect(jsonPath("$.data[0].rank").value(1))
                .andExpect(jsonPath("$.data[0].wins").value(86))
                .andExpect(jsonPath("$.data[1].rank").value(2))
                .andExpect(jsonPath("$.data[2].rank").value(3));
    }

    private TeamResponse.Detail createTeamResponse(TeamInfo.Team team, int rank,
                                                   int wins, int draws, int losses, double gamesBehind) {
        TeamRecord record = TeamRecord.builder()
                .teamId(team.id)
                .rank(rank)
                .gamesPlayed(wins + draws + losses)
                .totalGames(144)
                .wins(wins)
                .draws(draws)
                .losses(losses)
                .gamesBehind(gamesBehind)
                .build();

        return TeamResponse.Detail.from(team, record);
    }
}