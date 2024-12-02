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
    List<Match> findByStatusAndHomeTeamIdOrStatusAndAwayTeamIdOrderByMatchTimeDesc(
            MatchStatus status1, Long homeTeamId,
            MatchStatus status2, Long awayTeamId
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
}
