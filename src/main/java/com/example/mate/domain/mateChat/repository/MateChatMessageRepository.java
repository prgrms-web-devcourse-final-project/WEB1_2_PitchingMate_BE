package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.document.MateChatMessage;
import org.springframework.data.mongodb.repository.MongoRepository;

public interface MateChatMessageRepository extends MongoRepository<MateChatMessage, String>, MateChatMessageRepositoryCustom {

}