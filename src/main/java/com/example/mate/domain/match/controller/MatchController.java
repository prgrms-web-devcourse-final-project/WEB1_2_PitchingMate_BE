package com.example.mate.domain.match.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@Slf4j
@RestController
@RequestMapping("/api/matches")
@Tag(name = "Match", description = "경기 관련 API")
@RequiredArgsConstructor
public class MatchController {
    private final MatchService matchService;

    @Operation(summary = "메인 배너 경기 조회")
    @GetMapping("/main")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getMainBannerMatches() {

        List<MatchResponse> matches = matchService.getMainBannerMatches();
        System.out.println(matches);
        return ResponseEntity.ok(ApiResponse.success(matches));
    }

    //
    @Operation(summary = "팀별 경기 조회")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getTeamMatches(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getTeamMatches(teamId)));
    }
}