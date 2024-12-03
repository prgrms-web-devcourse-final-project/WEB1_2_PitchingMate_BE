package com.example.mate.domain.mateChat.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.mateChat.message.MessageType;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "chat_message")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MateChatMessage extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private MateChatRoom mateChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "sender_id")  // 시스템 메시지는 sender가 null일 수 있음
    private Member sender;

    @Enumerated(EnumType.STRING)
    @Column(name = "type", nullable = false)
    private MessageType type;

    @Column(name = "content", nullable = false, columnDefinition = "TEXT")
    private String content;

    private MateChatMessage(MateChatRoom mateChatRoom, Member sender, MessageType type,
                        String content) {
        this.mateChatRoom = mateChatRoom;
        this.sender = sender;
        this.type = type;
        this.content = content;
    }
}
