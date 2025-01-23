package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface GoodsChatMessageRepository
        extends MongoRepository<GoodsChatMessage, String>, GoodsChatMessageRepositoryCustom {

    /**
     * 특정 채팅방에 속한 모든 메시지를 삭제합니다.
     * @param chatRoomId 삭제할 채팅방 ID
     */
    void deleteAllByChatRoomId(Long chatRoomId);
}
