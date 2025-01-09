package com.example.mate.domain.crawler.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.constant.StadiumInfo;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.crawler.constant.GameType;
import com.example.mate.domain.crawler.dto.CrawlingStatus;
import com.example.mate.domain.crawler.dto.CrawlingStatusResponse;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.match.entity.MatchStatus;
import com.example.mate.domain.match.entity.TeamRecord;
import com.example.mate.domain.match.repository.MatchRepository;
import com.example.mate.domain.match.repository.TeamRecordRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.openqa.selenium.By;
import org.openqa.selenium.WebDriver;
import org.openqa.selenium.WebElement;
import org.openqa.selenium.support.ui.Select;
import org.springframework.beans.factory.ObjectFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.*;
import java.util.stream.Collectors;

@Service
@Slf4j
@RequiredArgsConstructor
public class CrawlingService {
    private final ObjectFactory<WebDriver> webDriverFactory;
    private final MatchRepository matchRepository;
    private final TeamRecordRepository teamRecordRepository;
    private final CrawlingStatus currentStatus = new CrawlingStatus();

    private String baseUrl = "https://www.koreabaseball.com/Schedule/Schedule.aspx";

    @Transactional
    public CrawlingStatusResponse crawlAllCurrentMatches() {
        log.info("Starting crawl for specific match types");
        currentStatus.start();
        WebDriver webDriver = null;
        int totalProcessedCount = 0;

        try {
            webDriver = webDriverFactory.getObject();
            LocalDate now = LocalDate.now().withYear(2025).withMonth(3); // 2025년 3월로 설정

            // 현재 월과 다음 달의 데이터 크롤링
            for (int monthOffset = 0; monthOffset < 2; monthOffset++) {
                LocalDate targetDate = now.plusMonths(monthOffset);
                List<GameType> gameTypes = getGameTypesByMonth(targetDate);

                // 경기가 없는 월은 건너뛰기
                if (gameTypes.isEmpty()) {
                    log.info("Skipping month {} as no games are scheduled", targetDate.getMonthValue());
                    continue;
                }

                for (GameType gameType : gameTypes) {
                    try {
                        log.info("Crawling schedule for date: Year={}, Month={}, GameType={}",
                                targetDate.getYear(),
                                targetDate.getMonthValue(),
                                gameType.getDescription());

                        List<Match> matches = crawlMonthlySchedule(webDriver, targetDate, gameType);
                        updateMatches(matches);
                        totalProcessedCount += matches.size();
                    } catch (Exception e) {
                        log.error("Error crawling {} matches for {}/{}",
                                gameType.getDescription(),
                                targetDate.getYear(),
                                targetDate.getMonthValue(),
                                e);
                    }
                }
            }

            if (totalProcessedCount == 0) {
                log.info("No games found for the current period. This is normal for off-season months.");
                currentStatus.complete(0);
                return currentStatus.toResponse();
            }

            currentStatus.complete(totalProcessedCount);
        } catch (Exception e) {
            log.error("Crawling failed with exception: ", e);
            currentStatus.fail(ErrorCode.CRAWLING_FAILED);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error closing WebDriver", e);
                }
            }
        }

        return currentStatus.toResponse();
    }

    @Transactional
    protected void updateMatches(List<Match> newMatches) {
        try {
            for (Match newMatch : newMatches) {
                Optional<Match> existingMatch = matchRepository.findByMatchTimeAndHomeTeamIdAndAwayTeamId(
                        newMatch.getMatchTime(),
                        newMatch.getHomeTeamId(),
                        newMatch.getAwayTeamId()
                );

                if (existingMatch.isPresent()) {
                    Match currentMatch = existingMatch.get();
                    if (hasMatchChanged(currentMatch, newMatch)) {
                        currentMatch.updateMatchDetails(
                                newMatch.getHomeScore(),
                                newMatch.getAwayScore(),
                                newMatch.getStatus(),
                                newMatch.getIsCanceled()
                        );
                        matchRepository.save(currentMatch);
                        log.info("Updated match details: id={}", currentMatch.getId());
                    }
                } else {
                    matchRepository.save(newMatch);
                    log.info("Saved new match: homeTeam={}, awayTeam={}, date={}",
                            newMatch.getHomeTeamId(),
                            newMatch.getAwayTeamId(),
                            newMatch.getMatchTime());
                }
            }
        } catch (Exception e) {
            log.error("Error updating matches", e);
            throw new CustomException(ErrorCode.MATCH_SAVE_ERROR);
        }
    }


    private boolean hasMatchChanged(Match currentMatch, Match newMatch) {
        // 점수 변경 확인
        boolean scoreChanged = !Objects.equals(currentMatch.getHomeScore(), newMatch.getHomeScore()) ||
                !Objects.equals(currentMatch.getAwayScore(), newMatch.getAwayScore());

        // 취소 상태 변경 확인
        boolean statusChanged = !Objects.equals(currentMatch.getIsCanceled(), newMatch.getIsCanceled()) ||
                !Objects.equals(currentMatch.getStatus(), newMatch.getStatus());

        if (scoreChanged || statusChanged) {
            log.info("Match updated - id: {}, homeTeam: {}, awayTeam: {}, " +
                            "previous [score: {} vs {}, canceled: {}, status: {}], " +
                            "new [score: {} vs {}, canceled: {}, status: {}]",
                    currentMatch.getId(),
                    currentMatch.getHomeTeamId(),
                    currentMatch.getAwayTeamId(),
                    currentMatch.getHomeScore(),
                    currentMatch.getAwayScore(),
                    currentMatch.getIsCanceled(),
                    currentMatch.getStatus(),
                    newMatch.getHomeScore(),
                    newMatch.getAwayScore(),
                    newMatch.getIsCanceled(),
                    newMatch.getStatus()
            );
        }

        return scoreChanged || statusChanged;
    }

    @Transactional
    public CrawlingStatusResponse crawlAllMatchesByDate(int year, int month) {
        log.info("Starting crawl for all match types for date: {}/{}", year, month);
        currentStatus.start();
        WebDriver webDriver = null;
        int totalProcessedCount = 0;

        try {
            webDriver = webDriverFactory.getObject();
            LocalDate targetDate = LocalDate.of(year, month, 1);

            // 모든 경기 타입에 대해 크롤링 수행
            for (GameType gameType : GameType.values()) {
                try {
                    List<Match> matches = crawlMonthlySchedule(webDriver, targetDate, gameType);
                    updateMatches(matches);
                    totalProcessedCount += matches.size();
                } catch (Exception e) {
                    log.error("Error crawling {} matches for {}/{}",
                            gameType.getDescription(), year, month, e);
                }
            }

            currentStatus.complete(totalProcessedCount);
        } catch (Exception e) {
            log.error("Crawling failed with exception: ", e);
            currentStatus.fail(ErrorCode.CRAWLING_FAILED);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error closing WebDriver", e);
                }
            }
        }

        return currentStatus.toResponse();
    }


    @Transactional
    protected void saveMatches(List<Match> matches) {
        try {
            matchRepository.saveAll(matches);
        } catch (Exception e) {
            log.error("Error saving matches", e);
            throw new CustomException(ErrorCode.MATCH_SAVE_ERROR);
        }
    }

    private List<Match> crawlMonthlySchedule(WebDriver webDriver, LocalDate date, GameType gameType) throws InterruptedException {
        try {
            webDriver.get(baseUrl);

            // 년도 선택
            WebElement yearSelect = webDriver.findElement(By.id("ddlYear"));
            Select yearSelector = new Select(yearSelect);
            yearSelector.selectByValue(String.valueOf(date.getYear()));
            Thread.sleep(1000);

            // 경기 타입 선택
            WebElement gameTypeSelector = webDriver.findElement(By.id("ddlSeries"));
            new Select(gameTypeSelector).selectByValue(gameType.getValue());
            Thread.sleep(1000);

            // 월 선택 - 2자리 문자열로 변환
            String monthValue = String.format("%02d", date.getMonthValue());
            WebElement monthSelect = webDriver.findElement(By.id("ddlMonth"));
            Select monthSelector = new Select(monthSelect);
            monthSelector.selectByValue(monthValue);
            Thread.sleep(1000);

            List<Match> matches = new ArrayList<>();
            String currentDay = null;

            List<WebElement> rows = webDriver.findElements(By.cssSelector("#tblScheduleList > tbody > tr"));
            for (WebElement row : rows) {
                try {
                    // 날짜 정보 갱신 (rowspan이 있는 경우)
                    List<WebElement> dayElements = row.findElements(By.className("day"));
                    if (!dayElements.isEmpty()) {
                        WebElement dayElement = dayElements.get(0);
                        String rowspanValue = dayElement.getAttribute("rowspan");
                        if (rowspanValue != null && !rowspanValue.isEmpty()) {
                            currentDay = dayElement.getText().replaceAll("[^0-9.]", "");
                        }
                    }

                    // 시간 정보가 있는 경우만 처리
                    List<WebElement> timeElements = row.findElements(By.className("time"));
                    if (!timeElements.isEmpty() && !timeElements.get(0).getText().equals("-")) {
                        Match match = parseMatchFromRow(row, currentDay, date, timeElements.get(0).getText());
                        if (match != null) {
                            matches.add(match);
                        }
                    }
                } catch (Exception e) {
                    log.error("Error parsing row: {}", e.getMessage());
                    // 한 행 파싱 실패시 다음 행으로 진행
                    continue;
                }
            }

            return matches;
        } catch (Exception e) {
            log.error("Error crawling schedule for {} - {}", gameType.getDescription(), date, e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        }
    }


    private String parseCancelReasonFromRow(WebElement row, boolean isCanceled) {
        try {
            if (isCanceled) {
                // 취소 사유는 항상 8번째 td에서 추출
                WebElement cancelReasonElement = row.findElement(By.cssSelector("td:nth-child(8)"));
                return cancelReasonElement.getText().trim();
            }
            return ""; // 정상 경기일 경우 빈 문자열 반환
        } catch (NoSuchElementException e) {
            log.warn("Cancel reason information missing for row: {}", row.getText());
            return ""; // 기본값 설정
        }
    }


    private Match parseMatchFromRow(WebElement row, String currentDay, LocalDate date, String time) {
        try {
            // 날짜 추출
            List<WebElement> dayElements = row.findElements(By.cssSelector("td.day"));
            boolean hasDayElement = !dayElements.isEmpty();
            if (hasDayElement) {
                currentDay = dayElements.get(0).getText();
            } else if (currentDay == null) {
                throw new CustomException(ErrorCode.DATE_NOT_FOUND);
            }

            // 경기 시간 추출
            WebElement timeElement = row.findElement(By.cssSelector("td.time"));
            String matchTimeText = timeElement.getText();

            // 취소 여부 및 취소 사유 확인
            List<WebElement> tdElements = row.findElements(By.cssSelector("td"));
            boolean isCanceled = false;
            String cancelReason = "-";
            if (tdElements.size() >= 8) {
                cancelReason = hasDayElement
                        ? tdElements.get(9 - 1).getText().trim()
                        : tdElements.get(8 - 1).getText().trim();

                if ("우천취소".equals(cancelReason) || "그라운드 사정".equals(cancelReason)) {
                    isCanceled = true;
                } else if ("-".equals(cancelReason)) {
                    cancelReason = "";
                }
            }

            // 팀 이름 추출
            WebElement team1Element = row.findElement(By.cssSelector("td.play > span"));
            String team1Name = team1Element.getText();
            WebElement team2Element = row.findElement(By.cssSelector("td.play > span:nth-child(3)"));
            String team2Name = team2Element.getText();

            // 점수 추출 및 경기 상태 결정
            Integer team1Score = null;
            Integer team2Score = null;
            MatchStatus matchStatus = MatchStatus.SCHEDULED; // 기본값을 SCHEDULED로 설정

            if (!isCanceled) {
                WebElement vsElement = row.findElement(By.cssSelector("td.play > em"));
                String vsText = vsElement.getText();
                String[] scores = vsText.split("vs");

                if (scores.length == 2 &&
                        (!scores[0].trim().isEmpty() || !scores[1].trim().isEmpty())) {
                    team1Score = scores[0].trim().isEmpty() ? 0 : Integer.parseInt(scores[0].trim());
                    team2Score = scores[1].trim().isEmpty() ? 0 : Integer.parseInt(scores[1].trim());
                    matchStatus = MatchStatus.COMPLETED;
                }
            } else {
                matchStatus = MatchStatus.CANCELED;
            }

            // 경기장 정보 파싱
            String stadium = parseStadiumFromRow(row, hasDayElement);

            // stadium을 기반으로 홈팀 결정
            Long team1Id = findTeamId(team1Name);
            Long team2Id = findTeamId(team2Name);
            StadiumInfo.Stadium currentStadium = StadiumInfo.getByName(stadium);

            // 현재 경기장이 team1의 홈구장인지 확인
            boolean isTeam1Home = TeamInfo.TEAMS.stream()
                    .filter(team -> team.id.equals(team1Id))
                    .anyMatch(team -> team.getHomeStadiums().stream()
                            .anyMatch(s -> s.name.equals(currentStadium.name)));

            // 홈팀/어웨이팀 ID와 스코어 설정
            Long homeTeamId, awayTeamId;
            Integer homeScore, awayScore;
            if (isTeam1Home) {
                homeTeamId = team1Id;
                awayTeamId = team2Id;
                homeScore = team1Score;
                awayScore = team2Score;
            } else {
                homeTeamId = team2Id;
                awayTeamId = team1Id;
                homeScore = team2Score;
                awayScore = team1Score;
            }

            // 날짜와 시간 파싱
            String[] timeParts = matchTimeText.split(":");
            String[] dateParts = currentDay.split("\\.");
            int day = Integer.parseInt(dateParts[1].replaceAll("[^0-9]", ""));
            LocalDateTime matchTime = LocalDateTime.of(
                    date.getYear(),
                    Integer.parseInt(dateParts[0]),
                    day,
                    Integer.parseInt(timeParts[0]),
                    Integer.parseInt(timeParts[1])
            );

            log.info("Parsed match: OriginalDate={}, ParsedDate={}, Time={}, Team1={} (Score={}), Team2={} (Score={}), Stadium={}, Status={}, IsCanceled={}, Reason={}",
                    currentDay, matchTime.toLocalDate(), matchTimeText,
                    TeamInfo.getById(team1Id).fullName, team1Score,
                    TeamInfo.getById(team2Id).fullName, team2Score,
                    stadium, matchStatus, isCanceled, cancelReason);

            return Match.builder()
                    .homeTeamId(homeTeamId)
                    .awayTeamId(awayTeamId)
                    .homeScore(homeScore)
                    .awayScore(awayScore)
                    .stadiumId(findStadiumId(stadium))
                    .matchTime(matchTime)
                    .status(matchStatus)
                    .isCanceled(isCanceled)
                    .build();

        } catch (Exception e) {
            log.error("Error parsing match data: {}", e.getMessage());
            throw new CustomException(ErrorCode.PARSING_ERROR);
        }
    }

    private String parseStadiumFromRow(WebElement row, boolean hasDayElement) {
        try {
            WebElement locationElement;

            if (hasDayElement) {
                // 날짜 정보가 rowspan으로 존재할 경우, 경기장 정보는 8번째 td
                locationElement = row.findElement(By.cssSelector("td:nth-child(8)"));
            } else {
                // 일반적인 경우, 경기장 정보는 7번째 td
                locationElement = row.findElement(By.cssSelector("td:nth-child(7)"));
            }

            String rawStadium = locationElement.getText().trim();

            return switch (rawStadium) {
                case "광주" -> "광주-기아 챔피언스 필드";
                case "잠실" -> "잠실야구장";
                case "창원" -> "창원 NC 파크";
                case "문학" -> "인천 SSG 랜더스필드";
                case "수원" -> "수원 kt wiz 파크";
                case "사직" -> "사직야구장";
                case "대구" -> "대구삼성라이온즈파크";
                case "고척" -> "고척스카이돔";
                case "대전" -> "한화생명이글스파크";
                case "울산" -> "울산 문수야구장";
                case "이천(두산)" -> "베어스 파크";
                case "청주" -> "청주야구장";
                default -> {
                    log.error("Stadium not found for name: {}", rawStadium);
                    yield ""; // 기본값 반환
                }
            };

        } catch (NoSuchElementException e) {
            log.warn("Stadium information missing for row: {}", row.getText());
            return ""; // 기본값 설정
        }
    }


    private Long findTeamId(String teamName) {
        String fullTeamName = switch (teamName) {
            case "삼성" -> "삼성 라이온즈";
            case "LG" -> "LG 트윈스";
            case "KT" -> "KT 위즈";
            case "NC" -> "NC 다이노스";
            case "SSG" -> "SSG 랜더스";
            case "롯데" -> "롯데 자이언츠";
            case "두산" -> "두산 베어스";
            case "KIA", "기아" -> "KIA 타이거즈";
            case "한화" -> "한화 이글스";
            case "키움" -> "키움 히어로즈";
            default -> teamName;
        };

        return TeamInfo.TEAMS.stream()
                .filter(team -> fullTeamName.contains(team.shortName) || fullTeamName.contains(team.fullName))
                .map(team -> team.id)
                .findFirst()
                .orElseThrow(() -> {
                    log.error("Team not found for name: {}", teamName);
                    return new CustomException(ErrorCode.TEAM_NOT_FOUND);
                });
    }

    private Long findStadiumId(String stadiumName) {
        return StadiumInfo.STADIUMS.stream()
                .filter(stadium -> stadiumName.contains(stadium.name))
                .map(stadium -> stadium.id)
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.STADIUM_NOT_FOUND_BY_NAME));
    }

    public CrawlingStatusResponse getStatus() {
        if (currentStatus == null) {
            log.warn("CrawlingStatus is null, creating new instance");
            return new CrawlingStatus().toResponse();
        }
        return currentStatus.toResponse();
    }

    public CrawlingStatusResponse crawlMatchesByDate(int year, int month, GameType gameType) {
        log.info("Starting crawl matches for date: Year={}, Month={}, GameType={}",
                year, month, gameType.getDescription());
        currentStatus.start();
        WebDriver webDriver = null;

        try {
            webDriver = webDriverFactory.getObject();
            LocalDate targetDate = LocalDate.of(year, month, 1);

            List<Match> matches = crawlMonthlySchedule(webDriver, targetDate, gameType);
            saveMatches(matches);

            currentStatus.complete(matches.size());
            return currentStatus.toResponse();

        } catch (Exception e) {
            log.error("Crawling failed with exception: ", e);
            currentStatus.fail(ErrorCode.CRAWLING_FAILED);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error closing WebDriver", e);
                }
            }
        }
    }

    /// 테스트
    @Transactional
    public CrawlingStatusResponse crawlMatchesFromCustomDate(LocalDate customDate) {
        log.info("Starting crawl for specific match types from custom date: {}", customDate);
        currentStatus.start();
        WebDriver webDriver = null;
        int totalProcessedCount = 0;

        try {
            webDriver = webDriverFactory.getObject();

            // 해당 월과 다음 달의 데이터 크롤링
            for (int monthOffset = 0; monthOffset < 2; monthOffset++) {
                LocalDate targetDate = customDate.plusMonths(monthOffset);
                List<GameType> gameTypes = getGameTypesByMonth(targetDate);

                // 경기가 없는 월은 건너뛰기
                if (gameTypes.isEmpty()) {
                    log.info("Skipping month {} as no games are scheduled", targetDate.getMonthValue());
                    continue;
                }

                for (GameType gameType : gameTypes) {
                    try {
                        log.info("Crawling schedule for date: Year={}, Month={}, GameType={}",
                                targetDate.getYear(),
                                targetDate.getMonthValue(),
                                gameType.getDescription());

                        List<Match> matches = crawlMonthlySchedule(webDriver, targetDate, gameType);
                        updateMatches(matches);
                        totalProcessedCount += matches.size();
                    } catch (Exception e) {
                        log.error("Error crawling {} matches for {}/{}",
                                gameType.getDescription(),
                                targetDate.getYear(),
                                targetDate.getMonthValue(),
                                e);
                    }
                }
            }

            if (totalProcessedCount == 0) {
                log.info("No games found for the specified period ({}). This is normal for off-season months.",
                        customDate.getMonth());
                currentStatus.complete(0);
                return currentStatus.toResponse();
            }

            currentStatus.complete(totalProcessedCount);
        } catch (Exception e) {
            log.error("Crawling failed with exception: ", e);
            currentStatus.fail(ErrorCode.CRAWLING_FAILED);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error closing WebDriver", e);
                }
            }
        }

        return currentStatus.toResponse();
    }

    private List<GameType> getGameTypesByMonth(LocalDate date) {
        int month = date.getMonthValue();
        List<GameType> gameTypes = new ArrayList<>();

        // 오프시즌 (12월, 1월, 2월)은 빈 리스트 반환
        if (month == 12 || month == 1 || month == 2) {
            log.info("Month {} is off-season", month);
            return gameTypes;
        }

        // 3월: 시범경기 + 정규시즌
        if (month == 3) {
            gameTypes.add(GameType.EXHIBITION);
            gameTypes.add(GameType.REGULAR);
        }
        // 4월~9월: 정규시즌만
        else if (month >= 4 && month <= 9) {
            gameTypes.add(GameType.REGULAR);
        }
        // 10월: 정규시즌 + 포스트시즌
        else if (month == 10) {
            gameTypes.add(GameType.REGULAR);
            gameTypes.add(GameType.POST);
        }
        // 11월: 포스트시즌만
        else if (month == 11) {
            gameTypes.add(GameType.POST);
        }

        log.info("Selected game types for month {}: {}", month,
                gameTypes.isEmpty() ? "none" :
                        gameTypes.stream()
                                .map(GameType::getDescription)
                                .collect(Collectors.joining(", ")));

        return gameTypes;
    }


    //////////// 팀 순위 크롤링 ////////////////

    public List<TeamRecord> crawlTeamRankings() {
        log.info("Starting crawl team rankings");
        WebDriver webDriver = null;
        List<TeamRecord> rankings = new ArrayList<>();

        try {
            webDriver = webDriverFactory.getObject();
            webDriver.get("https://sports.news.naver.com/kbaseball/record/index?category=kbo");

            // 특정 테이블만 선택
            WebElement table = webDriver.findElement(By.cssSelector("#content > div.tb_kbo > div > div:nth-child(3)"));
            List<WebElement> bodyRows = table.findElements(By.cssSelector("tbody tr"));

            log.info("Found {} rows in the table.", bodyRows.size());

            for (WebElement row : bodyRows) {
                List<WebElement> cells = row.findElements(By.cssSelector("td"));

                // 크롤링한 데이터 로그 출력
                String teamName = cells.get(0).getText().trim();
                log.info("Crawled raw team name: {}", teamName);

                try {
                    // 팀명으로 TeamInfo에서 팀 객체 찾기
                    TeamInfo.Team team = TeamInfo.findByFullName(teamName);
                    log.info("Mapped team name '{}' to TeamInfo.Team: {}", teamName, team);

                    // 경기수, 승, 패, 무
                    Integer gamesPlayed = Integer.parseInt(cells.get(1).getText().trim());
                    Integer wins = Integer.parseInt(cells.get(2).getText().trim());
                    Integer losses = Integer.parseInt(cells.get(3).getText().trim());
                    Integer draws = Integer.parseInt(cells.get(4).getText().trim());

                    // 승률, 게임차
                    Double winningRate = Double.parseDouble(cells.get(5).getText().trim());
                    Double gamesBehind = parseGamesBehind(cells.get(6).getText().trim());

                    // 팀 순위 계산 (인덱스 + 1)
                    Integer rank = bodyRows.indexOf(row) + 1;

                    log.info("Parsed stats for team '{}': Rank: {}, Games Played: {}, Wins: {}, Losses: {}, Draws: {}, GB: {}",
                            teamName, rank, gamesPlayed, wins, losses, draws, gamesBehind);

                    TeamRecord record = TeamRecord.builder()
                            .teamId(team.id)
                            .rank(rank)
                            .gamesPlayed(gamesPlayed)
                            .totalGames(144)  // KBO 정규시즌 경기 수
                            .wins(wins)
                            .losses(losses)
                            .draws(draws)
                            .gamesBehind(gamesBehind)
                            .build();

                    rankings.add(record);

                    // 기존 데이터 조회 또는 업데이트
                    TeamRecord teamRecord = teamRecordRepository.findByTeamId(team.id)
                            .orElse(record);

                    teamRecord.updateRecord(rank, wins, draws, losses, gamesBehind);

                    log.info("Saving team record for team '{}': {}", teamName, teamRecord);

                    teamRecordRepository.save(teamRecord);

                } catch (CustomException e) {
                    log.error("Failed to map team name '{}' to TeamInfo.Team: {}", teamName, e.getMessage());
                } catch (Exception ex) {
                    log.error("Unexpected error while processing team '{}': {}", teamName, ex.getMessage());
                }
            }

        } catch (Exception e) {
            log.error("Error crawling team rankings: ", e);
            throw new CustomException(ErrorCode.CRAWLING_FAILED);
        } finally {
            if (webDriver != null) {
                try {
                    webDriver.quit();
                } catch (Exception e) {
                    log.error("Error closing WebDriver", e);
                }
            }
        }

        log.info("Crawling completed. Total teams processed: {}", rankings.size());
        return rankings;
    }


    private Double parseGamesBehind(String gamesBehind) {
        if (gamesBehind.equals("-") || gamesBehind.equals("0.0")) {
            return 0.0;
        }
        return Double.parseDouble(gamesBehind);
    }

}