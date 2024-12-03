package com.example.mate.domain.match.service;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import java.util.List;
import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
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

    @Nested
    @DisplayName("팀 순위 조회")
    class GetTeamRankings {
        @Test
        @DisplayName("전체 팀 순위 조회 성공")
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

        @Test
        @DisplayName("전체 팀 순위 조회 - 데이터 없음")
        void getTeamRankings_EmptyList() {
            // Given
            when(teamRecordRepository.findAllByOrderByRankAsc()).thenReturn(List.of());

            // When
            List<TeamResponse.Detail> result = teamService.getTeamRankings();

            // Then
            assertThat(result).isEmpty();
            verify(teamRecordRepository).findAllByOrderByRankAsc();
        }
    }

    @Nested
    @DisplayName("특정 팀 순위 조회")
    class GetTeamRanking {
        @Test
        @DisplayName("특정 팀 순위 조회 성공")
        void getTeamRanking_Success() {
            // Given
            TeamRecord teamRecord = TeamRecord.builder()
                    .teamId(TeamInfo.LG.id)
                    .rank(1)
                    .gamesPlayed(144)
                    .totalGames(144)
                    .wins(86)
                    .draws(2)
                    .losses(56)
                    .gamesBehind(0.0)
                    .build();

            when(teamRecordRepository.findByTeamId(TeamInfo.LG.id)).thenReturn(Optional.of(teamRecord));

            // When
            TeamResponse.Detail result = teamService.getTeamRanking(TeamInfo.LG.id);

            // Then
            assertThat(result.getRank()).isEqualTo(1);
            assertThat(result.getWins()).isEqualTo(86);
            assertThat(result.getDraws()).isEqualTo(2);
            assertThat(result.getLosses()).isEqualTo(56);
            verify(teamRecordRepository).findByTeamId(TeamInfo.LG.id);
        }

        @Test
        @DisplayName("특정 팀 순위 조회 실패 - 팀이 존재하지 않음")
        void getTeamRanking_TeamNotFound() {
            // Given
            when(teamRecordRepository.findByTeamId(999L)).thenReturn(Optional.empty());

            // When & Then
            assertThrows(CustomException.class, () ->
                    teamService.getTeamRanking(999L)
            );
            verify(teamRecordRepository).findByTeamId(999L);
        }
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