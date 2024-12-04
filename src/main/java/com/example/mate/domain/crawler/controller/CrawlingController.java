package com.example.mate.domain.crawler.controller;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.response.ApiResponse;
import com.example.mate.domain.crawler.constant.GameType;
import com.example.mate.domain.crawler.dto.CrawlingStatusResponse;
import com.example.mate.domain.crawler.service.CrawlingService;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.constraints.Min;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.hibernate.validator.constraints.Range;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/admin/crawling")
@Tag(name = "Crawling", description = "크롤링 관련 API")
@RequiredArgsConstructor
@Slf4j
public class CrawlingController {
    private final CrawlingService crawlingService;
    private final TeamRecordRepository teamRecordRepository;

    @Operation(summary = "현재 월과 다음 월 전체 경기 일정 수동 크롤링",
            description = "현재 월과 다음월의 모든 종류(정규/포스트/시범)의 경기 일정을 크롤링하고 변경사항을 업데이트합니다.")
    @PostMapping("/matches")
    public ResponseEntity<ApiResponse<CrawlingStatusResponse>> crawlCurrentMonthMatches() {
        try {
            CrawlingStatusResponse status = crawlingService.crawlAllCurrentMatches();
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (CustomException e) {
            log.error("Crawling failed: {}", e.getErrorCode().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during crawling", e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
    }

    @Operation(summary = "특정 월 전체 경기 일정 수동 크롤링",
            description = "지정된 연도/월의 모든 종류의 경기 일정을 크롤링하고 변경사항을 업데이트합니다.")
    @PostMapping("/matches/{year}/{month}")
    public ResponseEntity<ApiResponse<CrawlingStatusResponse>> crawlSpecificMonthMatches(
            @PathVariable @Min(2024) int year,
            @PathVariable @Range(min = 1, max = 12) int month) {
        try {
            CrawlingStatusResponse status = crawlingService.crawlAllMatchesByDate(year, month);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (CustomException e) {
            log.error("Crawling failed for date {}/{}: {}",
                    year, month, e.getErrorCode().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during crawling for date {}/{}", year, month, e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
    }

    @Operation(summary = "크롤링 상태 조회",
            description = "가장 최근 크롤링 작업의 상태를 조회합니다.")
    @GetMapping("/status")
    public ResponseEntity<ApiResponse<CrawlingStatusResponse>> getCrawlingStatus() {
        CrawlingStatusResponse status = crawlingService.getStatus();
        return ResponseEntity.ok(ApiResponse.success(status));
    }

    @Operation(summary = "특정 날짜 경기 일정 크롤링",
            description = "지정된 연도와 월의 경기 일정을 크롤링합니다.")
    @PostMapping("/matches/{gameType}/{year}/{month}")
    public ResponseEntity<ApiResponse<CrawlingStatusResponse>> crawlMatchesByDate(
            @PathVariable GameType gameType,
            @PathVariable int year,
            @PathVariable @Range(min = 1, max = 12) int month) {
        try {
            CrawlingStatusResponse status = crawlingService.crawlMatchesByDate(year, month, gameType);
            return ResponseEntity.ok(ApiResponse.success(status));
        } catch (CustomException e) {
            log.error("Crawling failed for date {}/{} game type {}: {}",
                    year, month, gameType, e.getErrorCode().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during crawling for date {}/{} game type {}",
                    year, month, gameType, e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
    }

    //테스트
    @PostMapping("/matches/custom")
    public ResponseEntity<CrawlingStatusResponse> crawlCustomDateMatches() {
        // 9월로 가정하고 크롤링
        LocalDate customDate = LocalDate.of(2024, 9, 1);
        return ResponseEntity.ok(crawlingService.crawlMatchesFromCustomDate(customDate));
    }

    // KBO 순위 크롤링
    @Operation(summary = "팀 순위 크롤링",
            description = "현재 팀 순위를 크롤링하고 데이터를 업데이트합니다.")
    @PostMapping("/rankings")
    public ResponseEntity<ApiResponse<List<TeamRecord>>> crawlTeamRankings() {
        try {
            List<TeamRecord> rankings = crawlingService.crawlTeamRankings();
            return ResponseEntity.ok(ApiResponse.success(rankings));
        } catch (CustomException e) {
            log.error("Crawling failed: {}", e.getErrorCode().getMessage(), e);
            throw e;
        } catch (Exception e) {
            log.error("Unexpected error during crawling", e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
    }

    @Operation(summary = "팀 순위 조회",
            description = "현재 저장된 팀 순위 정보를 조회합니다.")
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<TeamRecord>>> getTeamRankings() {
        List<TeamRecord> rankings = teamRecordRepository.findAllByOrderByRankAsc();
        return ResponseEntity.ok(ApiResponse.success(rankings));
    }
}