package com.example.mate.domain.match.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.dto.response.WeeklyMatchesResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.repository.MatchRepository;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.AssertionsForInterfaceTypes.assertThat;
import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
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
        LocalDateTime now = LocalDateTime.now();
        List<Match> matches = createTestMatches();
        when(matchRepository.findMainBannerMatches(any(LocalDateTime.class), any(Pageable.class)))
                .thenReturn(matches);

        // When
        List<MatchResponse> result = matchService.getMainBannerMatches();

        // Then
        assertThat(result).hasSize(5);
        verify(matchRepository).findMainBannerMatches(
                any(LocalDateTime.class),
                eq(PageRequest.of(0, 5))
        );
    }

    @Test
    @DisplayName("특정 팀의 상위 3개 경기를 조회")
    void getTeamMatches_ShouldReturnTop3MatchesForTeam() {
        // Given
        Long teamId = 1L;
        List<Match> matches = createTestMatches().subList(0, 3);
        when(matchRepository.findTop3TeamMatchesAfterNow(
                eq(teamId),
                any(LocalDateTime.class),
                any(Pageable.class)
        )).thenReturn(matches);

        // When
        List<MatchResponse> result = matchService.getTeamMatches(teamId);

        // Then
        assertThat(result).hasSize(3);
        verify(matchRepository).findTop3TeamMatchesAfterNow(
                eq(teamId),
                any(LocalDateTime.class),
                eq(PageRequest.of(0, 3))
        );
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
    @DisplayName("팀별 완료된 경기 조회 - 성공 (최대 6개)")
    void getTeamCompletedMatches_Success() {
        // given
        Long teamId = TeamInfo.LG.id;
        List<Match> completedMatches = new ArrayList<>();

        for (int i = 0; i < 7; i++) {
            completedMatches.add(createCompletedMatch(
                    TeamInfo.LG.id,
                    TeamInfo.KT.id,
                    5 + i,
                    3 + i
            ));
        }

        when(matchRepository.findRecentCompletedMatches(
                MatchStatus.COMPLETED, teamId,
                MatchStatus.COMPLETED, teamId))
                .thenReturn(completedMatches.subList(0, 6));

        // when
        List<MatchResponse> result = matchService.getTeamCompletedMatches(teamId);

        // then
        assertThat(result).hasSize(6);
        verify(matchRepository).findRecentCompletedMatches(
                MatchStatus.COMPLETED, teamId,
                MatchStatus.COMPLETED, teamId);
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 - 성공")
    void getTeamWeeklyMatches_Success() {
        // Given
        Long teamId = TeamInfo.LG.id;
        LocalDate queryDate = LocalDate.of(2024, 3, 25); // 월요일
        LocalDateTime startDateTime = queryDate.atStartOfDay();
        LocalDateTime endDateTime = queryDate.plusWeeks(4).atTime(LocalTime.MAX);

        List<Match> mockMatches = Arrays.asList(
                createMatch(teamId, queryDate.plusDays(1)),
                createMatch(teamId, queryDate.plusDays(8)),
                createMatch(teamId, queryDate.plusDays(15)),
                createMatch(teamId, queryDate.plusDays(22))
        );

        when(matchRepository.findTeamMatchesInPeriod(teamId, startDateTime, endDateTime))
                .thenReturn(mockMatches);

        // When
        List<WeeklyMatchesResponse> result = matchService.getTeamWeeklyMatches(teamId, queryDate);

        // Then
        assertThat(result)
                .hasSize(4)
                .satisfies(responses -> {
                    WeeklyMatchesResponse firstWeek = responses.get(0);
                    assertThat(firstWeek)
                            .extracting(
                                    WeeklyMatchesResponse::getWeekNumber,
                                    WeeklyMatchesResponse::getWeekLabel,
                                    r -> r.getMatches().size()
                            )
                            .containsExactly(1, "3월 4주차", 1);

                    assertThat(firstWeek.getWeekStartDate())
                            .isEqualTo(queryDate);
                    assertThat(firstWeek.getWeekEndDate())
                            .isEqualTo(queryDate.plusDays(6));
                });

        verify(matchRepository).findTeamMatchesInPeriod(teamId, startDateTime, endDateTime);
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 조회 - 존재하지 않는 팀")
    void getTeamWeeklyMatches_TeamNotFound() {
        // Given
        Long invalidTeamId = 999L;
        LocalDate queryDate = LocalDate.now();

        // When & Then
        assertThatThrownBy(() -> matchService.getTeamWeeklyMatches(invalidTeamId, queryDate))
                .isInstanceOf(CustomException.class)
                .hasFieldOrPropertyWithValue("errorCode", ErrorCode.TEAM_NOT_FOUND);
    }

    @Test
    @DisplayName("팀별 주차별 경기 일정 - 각 주차의 경기가 올바르게 그룹핑되는지 확인")
    void getTeamWeeklyMatches_CorrectGrouping() {
        // Given
        Long teamId = TeamInfo.LG.id;
        LocalDate queryDate = LocalDate.of(2024, 3, 25); // 월요일

        List<Match> mockMatches = Arrays.asList(
                createMatch(teamId, queryDate.plusDays(1)),  // 1주차
                createMatch(teamId, queryDate.plusDays(2)),  // 1주차
                createMatch(teamId, queryDate.plusDays(8)),  // 2주차
                createMatch(teamId, queryDate.plusDays(15)), // 3주차
                createMatch(teamId, queryDate.plusDays(22))  // 4주차
        );

        when(matchRepository.findTeamMatchesInPeriod(
                any(Long.class),
                any(LocalDateTime.class),
                any(LocalDateTime.class)
        )).thenReturn(mockMatches);

        // When
        List<WeeklyMatchesResponse> result = matchService.getTeamWeeklyMatches(teamId, queryDate);

        // Then
        assertThat(result)
                .hasSize(4)
                .satisfies(responses -> {
                    // 1주차 검증
                    assertThat(responses.get(0))
                            .satisfies(week -> {
                                assertThat(week.getWeekNumber()).isEqualTo(1);
                                assertThat(week.getMatches()).hasSize(2);
                                assertThat(week.getMatches())
                                        .extracting(match -> match.getMatchTime().toLocalDate())
                                        .containsExactly(
                                                queryDate.plusDays(1),
                                                queryDate.plusDays(2)
                                        );
                            });

                    // 2주차 검증
                    assertThat(responses.get(1))
                            .satisfies(week -> {
                                assertThat(week.getWeekNumber()).isEqualTo(2);
                                assertThat(week.getMatches()).hasSize(1);
                            });

                    // 3주차 검증
                    assertThat(responses.get(2))
                            .satisfies(week -> {
                                assertThat(week.getWeekNumber()).isEqualTo(3);
                                assertThat(week.getMatches()).hasSize(1);
                            });

                    // 4주차 검증
                    assertThat(responses.get(3))
                            .satisfies(week -> {
                                assertThat(week.getWeekNumber()).isEqualTo(4);
                                assertThat(week.getMatches()).hasSize(1);
                            });
                });
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

    private Match createMatch(Long teamId, LocalDate matchDate) {
        return Match.builder()
                .homeTeamId(teamId)
                .awayTeamId(TeamInfo.KT.id)  // 테스트용 상대팀
                .stadiumId(StadiumInfo.JAMSIL.id)  // 테스트용 구장
                .matchTime(matchDate.atTime(18, 30))  // 테스트용 경기 시간 (저녁 6시 30분)
                .status(MatchStatus.SCHEDULED)
                .isCanceled(false)
                .build();
    }
}