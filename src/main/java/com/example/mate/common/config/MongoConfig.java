package com.example.mate.common.config;

import com.example.mate.common.util.converter.DateToLocalDateTimeKstConverter;
import com.example.mate.common.util.converter.LocalDateTimeToDateKstConverter;
import java.util.List;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.data.mongodb.MongoDatabaseFactory;
import org.springframework.data.mongodb.core.convert.DbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultDbRefResolver;
import org.springframework.data.mongodb.core.convert.DefaultMongoTypeMapper;
import org.springframework.data.mongodb.core.convert.MappingMongoConverter;
import org.springframework.data.mongodb.core.convert.MongoCustomConversions;
import org.springframework.data.mongodb.core.mapping.MongoMappingContext;

@Configuration
public class MongoConfig {

    @Bean
    public MappingMongoConverter mappingMongoConverter(
            MongoDatabaseFactory mongoDatabaseFactory,
            MongoMappingContext mongoMappingContext,
            LocalDateTimeToDateKstConverter dateKstConverter,
            DateToLocalDateTimeKstConverter localDateTimeKstConverter
    ) {
        DbRefResolver dbRefResolver = new DefaultDbRefResolver(mongoDatabaseFactory);
        MappingMongoConverter converter = new MappingMongoConverter(dbRefResolver, mongoMappingContext);

        // "_class" 타입을 저장하지 않도록 설정
        converter.setTypeMapper(new DefaultMongoTypeMapper(null));

        // MongoDB KST 변환 컨버터 설정
        converter.setCustomConversions(new MongoCustomConversions(
                List.of(localDateTimeKstConverter, dateKstConverter)
        ));

        return converter;
    }
}
