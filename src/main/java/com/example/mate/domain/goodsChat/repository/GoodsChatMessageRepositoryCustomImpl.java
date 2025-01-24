package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import java.time.LocalDateTime;
import java.util.List;
import lombok.RequiredArgsConstructor;
import org.springframework.data.domain.Sort;
import org.springframework.data.domain.Sort.Direction;
import org.springframework.data.mongodb.core.MongoTemplate;
import org.springframework.data.mongodb.core.query.Criteria;
import org.springframework.data.mongodb.core.query.Query;

@RequiredArgsConstructor
public class GoodsChatMessageRepositoryCustomImpl implements GoodsChatMessageRepositoryCustom {

    private final MongoTemplate mongoTemplate;

    /**
     * 주어진 chatRoomId의 메시지 중에서
     * lastSentAt 보다 오래된 메시지를 최대 size 만큼 반환
     * 메시지는 sent_at 기준으로 내림차순 정렬됩니다.
     */
    @Override
    public List<GoodsChatMessage> getChatMessages(Long chatRoomId, LocalDateTime lastSentAt, int size) {
        // 동적으로 조건 생성
        Criteria criteria = createCriteria(chatRoomId, lastSentAt);

        // Query 생성 및 조건 추가
        Query query = new Query(criteria);
        query.limit(size);
        query.with(Sort.by(Direction.DESC, "sent_at"));

        return mongoTemplate.find(query, GoodsChatMessage.class);
    }

    private Criteria createCriteria(Long chatRoomId, LocalDateTime lastSentAt) {
        Criteria criteria = Criteria.where("chat_room_id").is(chatRoomId);

        // lastSentAt 가 null 일 경우, 최신 메시지 조회
        if (lastSentAt != null) {
            criteria = criteria.and("sent_at").lt(lastSentAt);
        }

        return criteria;
    }
}
