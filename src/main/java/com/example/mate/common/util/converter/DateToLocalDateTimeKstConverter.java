package com.example.mate.common.util.converter;

import java.time.LocalDateTime;
import java.time.ZoneId;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.ReadingConverter;
import org.springframework.stereotype.Component;

@Component
@ReadingConverter
public class DateToLocalDateTimeKstConverter implements Converter<Date, LocalDateTime> {

    private static final int KST_OFFSET_HOURS = 9;

    @Override
    public LocalDateTime convert(Date source) {
        return convertToKst(source);
    }

    // KST 로 변환하기 위해 9시간을 빼줌
    private LocalDateTime convertToKst(Date date) {
        LocalDateTime localDateTime = date.toInstant().atZone(ZoneId.systemDefault()).toLocalDateTime();
        return localDateTime.minusHours(KST_OFFSET_HOURS);
    }
}
