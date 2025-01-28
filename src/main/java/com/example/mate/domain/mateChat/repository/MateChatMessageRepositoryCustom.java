package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.document.MateChatMessage;

import java.time.LocalDateTime;
import java.util.List;

public interface MateChatMessageRepositoryCustom {

    // 마지막 입장시간 이후 메세지 조회
    List<MateChatMessage> getChatMessages(Long roomId, LocalDateTime lastEnterTime, LocalDateTime lastSentAt);
}
