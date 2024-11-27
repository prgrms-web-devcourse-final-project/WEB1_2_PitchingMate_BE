package com.example.mate.domain.match.service;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.repository.MatchRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.Comparator;
import java.util.List;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class MatchService {
    private final MatchRepository matchRepository;

    public List<MatchResponse> getMainBannerMatches() {
        return matchRepository.findTop5ByOrderByMatchTimeDesc().stream()
                .filter(match -> match.getMatchTime().isAfter(LocalDateTime.now()))
                .map(match -> MatchResponse.from(match, null))
                .collect(Collectors.toList());
    }


    public List<MatchResponse> getTeamMatches(Long teamId) {
        TeamInfo.getById(teamId);

        return matchRepository.findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(teamId, teamId).stream()
                .filter(match -> match.getMatchTime().isAfter(LocalDateTime.now()))
                .map(match -> MatchResponse.from(match, teamId))
                .collect(Collectors.toList());
    }

    public List<MatchResponse> getTeamCompletedMatches(Long teamId) {
        TeamInfo.getById(teamId);

        return matchRepository.findByStatusAndHomeTeamIdOrStatusAndAwayTeamIdOrderByMatchTimeDesc(
                        MatchStatus.COMPLETED, teamId,
                        MatchStatus.COMPLETED, teamId)
                .stream()
                .map(match -> MatchResponse.from(match, teamId))
                .collect(Collectors.toList());
    }
}

