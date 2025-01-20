package com.example.mate.domain.mateChat.document;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.mateChat.message.MessageType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.mapping.Document;

import java.time.LocalDateTime;

@Document(collection = "mate_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MateChatMessage extends BaseTimeEntity {
    @Id
    private String id;

    private Long roomId;
    private Long senderId;
    private MessageType type;
    private String content;
    private LocalDateTime sendTime;
}
