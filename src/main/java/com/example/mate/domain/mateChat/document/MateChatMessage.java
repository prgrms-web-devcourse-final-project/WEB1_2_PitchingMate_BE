package com.example.mate.domain.mateChat.document;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.mateChat.message.MessageType;
import jakarta.persistence.Id;
import lombok.*;
import org.springframework.data.mongodb.core.index.CompoundIndex;
import org.springframework.data.mongodb.core.index.CompoundIndexes;
import org.springframework.data.mongodb.core.mapping.Document;
import org.springframework.data.mongodb.core.mapping.Field;

import java.time.LocalDateTime;

@Document(collection = "mate_chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@CompoundIndexes({
        @CompoundIndex(name = "idx_chat_room_id_sent_at", def = "{ 'chat_room_id': 1, 'sent_at': -1 }")
}
)
public class MateChatMessage extends BaseTimeEntity {
    @Id
    private String id;

    @Field(name = "room_id")
    private Long roomId;

    @Field(name = "sender_id")
    private Long senderId;

    @Field(name = "content")
    private String content;

    @Field(name = "send_time")
    private LocalDateTime sendTime;


    @Field(name = "type")
    private MessageType type;
}
