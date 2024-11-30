package com.example.mate.domain.match.util;

import lombok.experimental.UtilityClass;

import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.temporal.TemporalAdjusters;
import java.time.temporal.WeekFields;

@UtilityClass
public class WeekCalculator {
    private static final WeekFields WEEK_FIELDS = WeekFields.of(DayOfWeek.MONDAY, 4);

    // 주의 시작일(월요일) 계산
    public LocalDate getWeekStart(LocalDate date) {
        return date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
    }

    // "n월 m주차" 형식의 라벨 생성
    public String generateWeekLabel(LocalDate date) {
        // 현재 주의 월요일 날짜를 구함
        LocalDate mondayOfWeek = date.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));

        // 월요일이 속한 달과 주차로 표시
        int month = mondayOfWeek.getMonthValue();
        int weekOfMonth = mondayOfWeek.get(WEEK_FIELDS.weekOfMonth());

        return String.format("%d월 %d주차", month, weekOfMonth);
    }

    // 시작일로부터 n주 후의 날짜 계산
    public LocalDate calculateEndDate(LocalDate startDate, int weekCount) {
        return startDate.plusWeeks(weekCount);
    }
}