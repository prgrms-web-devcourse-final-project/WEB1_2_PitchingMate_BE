package com.example.mate.domain.match.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Service
@Transactional(readOnly = true)
@RequiredArgsConstructor
public class TeamService {
    private final TeamRecordRepository teamRecordRepository;

    public List<TeamResponse.Detail> getTeamRankings() {
        List<TeamRecord> teamRecords = teamRecordRepository.findAllByOrderByRankAsc();

        return teamRecords.stream()
                .map(record -> {
                    TeamInfo.Team team = TeamInfo.getById(record.getTeamId());
                    return TeamResponse.Detail.from(team, record);
                })
                .collect(Collectors.toList());
    }

    public TeamResponse.Detail getTeamRanking(Long teamId) {
        TeamRecord teamRecord = teamRecordRepository.findByTeamId(teamId)
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));

        TeamInfo.Team team = TeamInfo.getById(teamId);
        return TeamResponse.Detail.from(team, teamRecord);
    }
}