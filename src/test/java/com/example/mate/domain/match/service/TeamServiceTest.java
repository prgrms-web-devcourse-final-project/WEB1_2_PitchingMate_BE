package com.example.mate.domain.match.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

@ExtendWith(MockitoExtension.class)
class TeamServiceTest {
    @Mock
    private TeamRecordRepository teamRecordRepository;
    @InjectMocks
    private TeamService teamService;

    @Test
    @DisplayName("팀 순위 조회 - 성공")
    void getTeamRankings_Success() {
        // Given
        List<TeamRecord> teamRecords = createTeamRecords();
        when(teamRecordRepository.findAllByOrderByRankAsc()).thenReturn(teamRecords);

        // When
        List<TeamResponse.Detail> result = teamService.getTeamRankings();

        // Then
        assertThat(result).hasSize(3);
        verify(teamRecordRepository).findAllByOrderByRankAsc();

        TeamResponse.Detail firstTeam = result.get(0);
        assertThat(firstTeam.getRank()).isEqualTo(1);
        assertThat(firstTeam.getWins()).isEqualTo(86);
        assertThat(firstTeam.getDraws()).isEqualTo(2);
        assertThat(firstTeam.getLosses()).isEqualTo(56);
    }

    private List<TeamRecord> createTeamRecords() {
        return List.of(
                TeamRecord.builder()
                        .teamId(TeamInfo.LG.id)
                        .rank(1)
                        .gamesPlayed(144)
                        .totalGames(144)
                        .wins(86)
                        .draws(2)
                        .losses(56)
                        .gamesBehind(0.0)
                        .build(),
                TeamRecord.builder()
                        .teamId(TeamInfo.KT.id)
                        .rank(2)
                        .gamesPlayed(144)
                        .totalGames(144)
                        .wins(81)
                        .draws(2)
                        .losses(61)
                        .gamesBehind(4.5)
                        .build(),
                TeamRecord.builder()
                        .teamId(TeamInfo.SSG.id)
                        .rank(3)
                        .gamesPlayed(144)
                        .totalGames(144)
                        .wins(76)
                        .draws(2)
                        .losses(66)
                        .gamesBehind(8.5)
                        .build()
        );
    }
}