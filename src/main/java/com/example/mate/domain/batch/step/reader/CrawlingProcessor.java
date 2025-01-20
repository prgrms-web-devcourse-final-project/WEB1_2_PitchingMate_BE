package com.example.mate.domain.batch.step.reader;

import com.example.mate.domain.crawler.service.CrawlingService;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class CrawlingProcessor implements ItemProcessor<String, String> {
    private final CrawlingService crawlingService;

    @Override
    public String process(String item) {
        crawlingService.crawlAllCurrentMatches();
        crawlingService.crawlTeamRankings();
        return item;
    }
}
