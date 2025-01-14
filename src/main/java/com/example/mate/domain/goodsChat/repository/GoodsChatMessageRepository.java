package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.data.mongodb.repository.Query;

public interface GoodsChatMessageRepository extends MongoRepository<GoodsChatMessage, String> {

    /**
     * 특정 채팅방의 메시지를 페이징 처리하여 조회합니다.
     * 메시지는 전송된 시간(sent_at) 기준으로 오름차순으로 정렬됩니다.
     */
    @Query(value = "{ 'chat_room_id': ?0 }", sort = "{ 'sent_at': -1 }")
    Page<GoodsChatMessage> getChatMessages(Long chatRoomId, Pageable pageable);

    void deleteAllByChatRoomId(Long chatRoomId);
}
