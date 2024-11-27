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
                .map(match -> MatchResponse.from(match, null))  // 메인 배너는 특정 팀 시점이 없으므로 null
                .collect(Collectors.toList());
    }


    public List<MatchResponse> getTeamMatches(Long teamId) {
        TeamInfo.getById(teamId);  // 팀 존재 여부 확인

        return matchRepository.findTop3ByHomeTeamIdOrAwayTeamIdOrderByMatchTimeDesc(teamId, teamId).stream()
                .filter(match -> match.getMatchTime().isAfter(LocalDateTime.now()))
                .map(match -> MatchResponse.from(match, teamId))  // 조회하는 팀 ID 전달
                .collect(Collectors.toList());
    }
}

