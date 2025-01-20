package com.example.mate.domain.batch.step.processor;

import org.springframework.batch.item.ItemReader;
import org.springframework.stereotype.Component;

@Component
public class CrawlingReader implements ItemReader<String> {
    private boolean completed = false;

    @Override
    public String read() {
        if (completed) {
            return null;
        }
        completed = true;
        return "CRAWLING_DATA";
    }
}
