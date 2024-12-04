package com.example.mate.domain.crawler.scheduler;

import com.example.mate.common.error.CustomException;
import com.example.mate.domain.crawler.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

@Component
@Slf4j
@RequiredArgsConstructor
public class CrawlingScheduler {
    private final CrawlingService crawlingService;

    @Scheduled(cron = "0 0 9 * * *", zone = "Asia/Seoul")
    public void morningUpdate() {
        log.info("Starting morning schedule update");
        try {
            // 현재 경기 크롤링
            crawlingService.crawlAllCurrentMatches();
            // 팀 순위 크롤링
            crawlingService.crawlTeamRankings();
        } catch (CustomException e) {
            log.error("Morning update failed: {}", e.getErrorCode().getMessage(), e);
        } catch (Exception e) {
            log.error("Morning update failed with unexpected error", e);
        }
    }

    @Scheduled(cron = "0 0 22 * * *", zone = "Asia/Seoul")
    public void eveningUpdate() {
        log.info("Starting evening schedule update");
        try {
            // 현재 경기 크롤링
            crawlingService.crawlAllCurrentMatches();
            // 팀 순위 크롤링
            crawlingService.crawlTeamRankings();
        } catch (CustomException e) {
            log.error("Evening update failed: {}", e.getErrorCode().getMessage(), e);
        } catch (Exception e) {
            log.error("Evening update failed with unexpected error", e);
        }
    }

    // 매 분 실행 자동화 테스트 (현재 off-season )
//    @Scheduled(cron = "0 * * * * *", zone = "Asia/Seoul")
//    public void testScheduler() {
//        log.info("Test Scheduler is running!");
//        try {
//            // 현재 경기 크롤링
//            crawlingService.crawlAllCurrentMatches();
//            // 팀 순위 크롤링
//            crawlingService.crawlTeamRankings();
//        } catch (CustomException e) {
//            log.error("Evening update failed: {}", e.getErrorCode().getMessage(), e);
//        } catch (Exception e) {
//            log.error("Evening update failed with unexpected error", e);
//        }
//    }
}
