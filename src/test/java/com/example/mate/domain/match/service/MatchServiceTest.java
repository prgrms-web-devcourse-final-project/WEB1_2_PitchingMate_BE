package com.example.mate.domain.match.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class MatchServiceTest {
    @Mock
    private MatchRepository matchRepository;
    @InjectMocks
    private MatchService matchService;

    @Test
    @DisplayName("메인 배너에 노출할 상위 5개 경기를 조회")
    void getMainBannerMatches_ShouldReturnTop5Matches() {
        // Given
        List<Match> matches = createTestMatches();
        when(matchRepository.findTop5ByOrderByMatchTimeDesc()).thenReturn(matches);

        // When
        List<MatchResponse> result = matchService.getMainBannerMatches();

        // Then
        assertThat(result).hasSize(5);
        verify(matchRepository).findTop5ByOrderByMatchTimeDesc();
    }

    @Test
    @DisplayName("특정 팀의 상위 3개 경기를 조회")
    void getTeamMatches_ShouldReturnTop3MatchesForTeam() {
        // Given
        Long teamId = 1L;
        List<Match> matches = createTestMatches().subList(0, 3);
        when(matchRepository.findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(teamId, teamId))
                .thenReturn(matches);

        // When
        List<MatchResponse> result = matchService.getTeamMatches(teamId);

        // Then
        assertThat(result).hasSize(3);
        verify(matchRepository).findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(teamId, teamId);
    }

    @Test
    @DisplayName("존재하지 않는 팀 ID로 조회 시 TEAM_NOT_FOUND 예외가 발생")
    void getTeamMatches_WithInvalidTeamId_ShouldThrowCustomException() {
        // Given
        Long invalidTeamId = 99L;

        // When & Then
        CustomException exception = assertThrows(CustomException.class,
                () -> matchService.getTeamMatches(invalidTeamId));
        assertEquals(ErrorCode.TEAM_NOT_FOUND, exception.getErrorCode());
    }

    @Test
    @DisplayName("팀별 완료된 경기 조회 - 성공")
    void getTeamCompletedMatches_Success() {
        // given
        Long teamId = TeamInfo.LG.id;
        List<Match> completedMatches = Arrays.asList(
                createCompletedMatch(TeamInfo.LG.id, TeamInfo.KT.id, 5, 3),
                createCompletedMatch(TeamInfo.KIA.id, TeamInfo.LG.id, 2, 7)
        );

        when(matchRepository.findByStatusAndHomeTeamIdOrStatusAndAwayTeamIdOrderByMatchTimeDesc(
                MatchStatus.COMPLETED, teamId, MatchStatus.COMPLETED, teamId))
                .thenReturn(completedMatches);

        // when
        List<MatchResponse> result = matchService.getTeamCompletedMatches(teamId);

        // then
        assertThat(result).hasSize(2);
        assertThat(result.get(0).getStatus()).isEqualTo(MatchStatus.COMPLETED);
        assertThat(result.get(0).getHomeTeam().getId()).isEqualTo(TeamInfo.LG.id);
        assertThat(result.get(0).getHomeScore()).isEqualTo(5);
        verify(matchRepository).findByStatusAndHomeTeamIdOrStatusAndAwayTeamIdOrderByMatchTimeDesc(
                MatchStatus.COMPLETED, teamId, MatchStatus.COMPLETED, teamId);
    }

    private Match createCompletedMatch(Long homeTeamId, Long awayTeamId, Integer homeScore, Integer awayScore) {
        return Match.builder()
                .homeTeamId(homeTeamId)
                .awayTeamId(awayTeamId)
                .stadiumId(StadiumInfo.JAMSIL.id)
                .matchTime(LocalDateTime.now().minusDays(1))
                .status(MatchStatus.COMPLETED)
                .homeScore(homeScore)
                .awayScore(awayScore)
                .isCanceled(false)
                .build();
    }


    private List<Match> createTestMatches() {
        return List.of(
                Match.builder()
                        .homeTeamId(TeamInfo.KIA.id)
                        .awayTeamId(TeamInfo.LG.id)
                        .stadiumId(StadiumInfo.GWANGJU.id)
                        .matchTime(LocalDateTime.now().plusDays(1))
                        .status(MatchStatus.SCHEDULED)
                        .build(),
                Match.builder()
                        .homeTeamId(TeamInfo.NC.id)
                        .awayTeamId(TeamInfo.KIA.id)
                        .stadiumId(StadiumInfo.CHANGWON.id)
                        .matchTime(LocalDateTime.now().plusDays(2))
                        .status(MatchStatus.SCHEDULED)
                        .build(),
                Match.builder()
                        .homeTeamId(TeamInfo.KT.id)
                        .awayTeamId(TeamInfo.DOOSAN.id)
                        .stadiumId(StadiumInfo.SUWON.id)
                        .matchTime(LocalDateTime.now().plusDays(3))
                        .status(MatchStatus.SCHEDULED)
                        .build(),
                Match.builder()
                        .homeTeamId(TeamInfo.SSG.id)
                        .awayTeamId(TeamInfo.SAMSUNG.id)
                        .stadiumId(StadiumInfo.INCHEON.id)
                        .matchTime(LocalDateTime.now().plusDays(4))
                        .status(MatchStatus.SCHEDULED)
                        .build(),
                Match.builder()
                        .homeTeamId(TeamInfo.HANWHA.id)
                        .awayTeamId(TeamInfo.LOTTE.id)
                        .stadiumId(StadiumInfo.DAEJEON.id)
                        .matchTime(LocalDateTime.now().plusDays(5))
                        .status(MatchStatus.SCHEDULED)
                        .build()
        );
    }
}