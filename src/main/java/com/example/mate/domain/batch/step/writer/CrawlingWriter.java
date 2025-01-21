package com.example.mate.domain.batch.step.writer;

import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.core.configuration.annotation.StepScope;
import org.springframework.batch.item.Chunk;
import org.springframework.batch.item.ItemWriter;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@StepScope
public class CrawlingWriter implements ItemWriter<String> {

    @Override
    public void write(Chunk<? extends String> chunk) throws Exception {
        log.info("Crawling completed: {}", chunk.getItems());
    }
}