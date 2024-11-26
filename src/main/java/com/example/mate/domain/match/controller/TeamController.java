package com.example.mate.domain.match.controller;

import com.example.mate.domain.match.dto.response.StadiumResponse;
import com.example.mate.domain.match.dto.response.TeamResponse;
import com.example.mate.common.response.ApiResponse;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.Arrays;
import java.util.List;

@Tag(name = "Team", description = "팀 관련 API")
@RestController
@RequestMapping("/api/teams")
@RequiredArgsConstructor
public class TeamController {
    private static final int TOTAL_GAMES = 144;  // KBO 정규시즌 총 경기 수

    @Operation(summary = "팀 순위 조회", description = "KBO 리그 전체 순위를 조회합니다.")
    @GetMapping("/rankings")
    public ResponseEntity<ApiResponse<List<TeamResponse.Detail>>> getTeamRankings() {
        List<TeamResponse.Detail> rankings = Arrays.asList(
                createTeamDetail(1L, "KIA 타이거즈", "광주-기아 챔피언스 필드", "광주광역시 북구", "126.8989", "35.1681",
                        100, 90, 10, 0, 2.0),
                createTeamDetail(2L, "LG 트윈스", "잠실야구장", "서울특별시 송파구", "127.0719", "37.5122",
                        95, 85, 10, 0, 3.5),
                createTeamDetail(3L, "NC 다이노스", "창원NC파크", "창원시 마산회원구", "128.5829", "35.2534",
                        92, 82, 10, 0, 4.0),
                createTeamDetail(4L, "SSG 랜더스", "인천SSG랜더스필드", "인천광역시 미추홀구", "126.6781", "37.4374",
                        90, 78, 12, 0, 5.0),
                createTeamDetail(5L, "kt wiz", "수원 kt wiz 파크", "수원시 장안구", "127.0355", "37.2994",
                        88, 75, 13, 0, 6.0),
                createTeamDetail(6L, "두산 베어스", "잠실야구장", "서울특별시 송파구", "127.0719", "37.5122",
                        86, 72, 14, 0, 7.0),
                createTeamDetail(7L, "롯데 자이언츠", "사직야구장", "부산광역시 동래구", "129.0639", "35.1947",
                        84, 68, 16, 0, 8.0),
                createTeamDetail(8L, "삼성 라이온즈", "대구삼성라이온즈파크", "대구광역시 수성구", "128.6814", "35.8409",
                        82, 65, 17, 0, 9.0),
                createTeamDetail(9L, "키움 히어로즈", "고척스카이돔", "서울특별시 구로구", "126.8669", "37.4982",
                        80, 62, 18, 0, 10.0),
                createTeamDetail(10L, "한화 이글스", "한화생명이글스파크", "대전광역시 중구", "127.4294", "36.3172",
                        78, 58, 20, 0, 11.0)
        );

        return ResponseEntity.ok(ApiResponse.success(rankings));
    }

    private TeamResponse.Detail createTeamDetail(Long id, String teamName,
                                                 String stadiumName, String location, String longitude, String latitude,
                                                 int gamesPlayed, int wins, int draws, int losses, double gamesBehind) {
        return TeamResponse.Detail.builder()
                .id(id)
                .teamName(teamName)
                .logoImageUrl(String.format("http://example.com/teams/%d/logo.png", id))
                .stadium(StadiumResponse.Info.builder()
                        .id(id)
                        .stadiumName(stadiumName)
                        .location(location)
                        .longitude(longitude)
                        .latitude(latitude)
                        .build())
                .rank(id.intValue())
                .gamesPlayed(gamesPlayed)
                .totalGames(TOTAL_GAMES)
                .wins(wins)
                .draws(draws)
                .losses(losses)
                .gamesBehind(gamesBehind)
                .build();
    }
}