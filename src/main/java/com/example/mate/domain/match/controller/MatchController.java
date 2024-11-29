package com.example.mate.domain.match.controller;

import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.match.dto.response.MatchResponse;
import com.example.mate.domain.match.dto.response.WeeklyMatchesResponse;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.service.MatchService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;
import java.util.Optional;

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

    @Operation(summary = "팀별 경기 조회")
    @GetMapping("/team/{teamId}")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getTeamMatches(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getTeamMatches(teamId)));
    }

    @Operation(summary = "팀별 완료된 경기 전적 조회")
    @GetMapping("/team/{teamId}/completed")
    public ResponseEntity<ApiResponse<List<MatchResponse>>> getTeamCompletedMatches(
            @Parameter(description = "팀 ID") @PathVariable Long teamId) {
        return ResponseEntity.ok(ApiResponse.success(matchService.getTeamCompletedMatches(teamId)));
    }

    @Operation(summary = "팀별 주차별 경기 일정 조회")
    @GetMapping("/team/{teamId}/weekly")
    public ResponseEntity<ApiResponse<List<WeeklyMatchesResponse>>> getTeamWeeklyMatches(
            @Parameter(description = "팀 ID")
            @PathVariable Long teamId,
            @Parameter(description = "조회 시작일 (yyyy-MM-dd), 입력하지 않으면 현재 날짜")
            @RequestParam(required = false)
            @DateTimeFormat(iso = DateTimeFormat.ISO.DATE)
            LocalDate startDate
    ) {
        LocalDate queryDate = Optional.ofNullable(startDate).orElse(LocalDate.now());
        return ResponseEntity.ok(ApiResponse.success(
                matchService.getTeamWeeklyMatches(teamId, queryDate)
        ));
    }
}