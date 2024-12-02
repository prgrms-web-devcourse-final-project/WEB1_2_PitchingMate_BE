package com.example.mate.domain.match.repository;

import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findTop5ByOrderByMatchTimeDesc();
    List<Match> findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(Long homeTeamId, Long awayTeamId);
    @Query("SELECT m FROM Match m " +
            "WHERE (m.status = :status1 AND m.homeTeamId = :homeTeamId) " +
            "OR (m.status = :status2 AND m.awayTeamId = :awayTeamId) " +
            "ORDER BY m.matchTime DESC " +
            "LIMIT 6")
    List<Match> findRecentCompletedMatches(
            @Param("status1") MatchStatus status1,
            @Param("homeTeamId") Long homeTeamId,
            @Param("status2") MatchStatus status2,
            @Param("awayTeamId") Long awayTeamId
    );
    @Query("SELECT m FROM Match m WHERE (m.homeTeamId = :teamId OR m.awayTeamId = :teamId) " +
            "AND m.matchTime BETWEEN :startDate AND :endDate " +
            "AND m.status = 'SCHEDULED' " +
            "AND m.isCanceled = false " +
            "ORDER BY m.matchTime ASC")
    List<Match> findTeamMatchesInPeriod(
            @Param("teamId") Long teamId,
            @Param("startDate") LocalDateTime startDate,
            @Param("endDate") LocalDateTime endDate
    );

    Optional<Match> findByMatchTimeAndHomeTeamIdAndAwayTeamId(
            LocalDateTime matchTime,
            Long homeTeamId,
            Long awayTeamId
    );

    List<Match> findByMatchTimeBetween(
            LocalDateTime startOfMonth,
            LocalDateTime endOfMonth
    );

    @Query("SELECT m FROM Match m " +
            "WHERE m.status = 'SCHEDULED' " +
            "AND m.matchTime BETWEEN :startTime AND :endTime " +
            "AND m.isCanceled = false " +
            "ORDER BY m.matchTime ASC")
    List<Match> findUpcomingMatches(
            @Param("startTime") LocalDateTime startTime,
            @Param("endTime") LocalDateTime endTime
    );
}
