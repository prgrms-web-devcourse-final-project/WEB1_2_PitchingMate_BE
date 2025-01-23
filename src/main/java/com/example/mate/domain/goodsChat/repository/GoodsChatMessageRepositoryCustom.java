package com.example.mate.domain.goodsChat.repository;

import com.example.mate.domain.goodsChat.document.GoodsChatMessage;
import java.time.LocalDateTime;
import java.util.List;

public interface GoodsChatMessageRepositoryCustom {
    List<GoodsChatMessage> getChatMessages(Long chatRoomId, LocalDateTime lastSentAt, int size);
}
