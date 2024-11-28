package com.example.mate.domain.match.repository;

import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MatchRepository extends JpaRepository<Match, Long> {
    List<Match> findTop5ByOrderByMatchTimeDesc();
    List<Match> findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(Long homeTeamId, Long awayTeamId);
    List<Match> findByStatusAndHomeTeamIdOrStatusAndAwayTeamIdOrderByMatchTimeDesc(
            MatchStatus status1, Long homeTeamId,
            MatchStatus status2, Long awayTeamId
    );
}
