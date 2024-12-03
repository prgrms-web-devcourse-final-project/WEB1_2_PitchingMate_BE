package com.example.mate.domain.match.controller;

import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.match.service.TeamService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Team", description = "팀 관련 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {

    private final TeamService teamService;
    @Operation(summary = "팀 순위 조회", description = "KBO 리그 전체 순위를 조회합니다.")
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<TeamResponse.Detail>>> getTeamRankings() {
        List<TeamResponse.Detail> rankings = teamService.getTeamRankings();
        return ResponseEntity.ok(ApiResponse.success(rankings));
    }

    @Operation(summary = "특정 팀 순위 조회", description = "특정 팀의 순위 정보를 조회합니다.")
    @GetMapping("/rankings/{teamId}")
    public ResponseEntity<ApiResponse<TeamResponse.Detail>> getTeamRanking(
            @Parameter(description = "팀 ID", required = true)
            @PathVariable Long teamId
    ) {
        TeamResponse.Detail ranking = teamService.getTeamRanking(teamId);
        return ResponseEntity.ok(ApiResponse.success(ranking));
    }
}