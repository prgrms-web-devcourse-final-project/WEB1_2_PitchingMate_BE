package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.document.MateChatMessage;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

import java.time.LocalDateTime;
import java.util.List;

@RequiredArgsConstructor
public class MateChatMessageRepositoryCustomImpl implements MateChatMessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;


    /**
     * 주어진 chatRoomId의 메시지 중에서
     * lastEnterTime 이후에 보내졌으며
     * lastSentAt 보다 오래된 메시지를 최대 20개 반환
     * 메시지는 send_time 기준으로 내림차순 정렬됩니다.
     */
    @Override
    public List<MateChatMessage> getChatMessages(Long roomId, LocalDateTime lastEnterTime, LocalDateTime lastSentAt) {
        // 동적으로 조건 생성
        Criteria criteria = createCriteria(roomId, lastEnterTime, lastSentAt);

        Query query = new Query(criteria);
        query.limit(20);
        query.with(Sort.by(Sort.Direction.DESC, "send_time"));

        return mongoTemplate.find(query, MateChatMessage.class);
    }

    private Criteria createCriteria(Long roomId, LocalDateTime lastEnterTime, LocalDateTime lastSentAt) {
        Criteria criteria = Criteria.where("room_id").is(roomId);

        if (lastSentAt != null) {
            criteria.andOperator(
                    Criteria.where("send_time").gt(lastEnterTime),
                    Criteria.where("send_time").lt(lastSentAt)
            );
        } else {
            criteria.and("send_time").gt(lastEnterTime);
        }

        return criteria;
    }
}
