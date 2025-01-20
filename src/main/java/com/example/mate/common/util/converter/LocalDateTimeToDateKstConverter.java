package com.example.mate.common.util.converter;

import java.sql.Timestamp;
import java.time.LocalDateTime;
import java.util.Date;
import org.springframework.core.convert.converter.Converter;
import org.springframework.data.convert.WritingConverter;
import org.springframework.stereotype.Component;

@Component
@WritingConverter
public class LocalDateTimeToDateKstConverter implements Converter<LocalDateTime, Date> {

    private static final int KST_OFFSET_HOURS = 9;

    @Override
    public Date convert(LocalDateTime source) {
        return convertToKst(source);
    }

    // KST 로 변환하기 위해 9시간을 더함
    private Date convertToKst(LocalDateTime localDateTime) {
        return Timestamp.valueOf(localDateTime.plusHours(KST_OFFSET_HOURS));
    }
}
