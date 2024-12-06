package com.example.mate.domain.mateChat.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;

@Entity
@Table(name = "mate_chat_room_member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MateChatRoomMember extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "chat_room_id", nullable = false)
    private MateChatRoom mateChatRoom;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id", nullable = false)
    private Member member;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "has_entered", nullable = false)
    @Builder.Default
    private Boolean hasEntered = false;  // 최초 입장 여부 추적

    @Column(name = "last_entered_at")
    private LocalDateTime lastEnteredAt;

    @Column(name = "last_read_message_id")
    private Long lastReadMessageId;

    // 최초 입장 시나 재입장 시 호출되는 메서드
    public void markAsEntered() {
        if (!this.hasEntered) {
            this.hasEntered = true;
        }
        this.lastEnteredAt = LocalDateTime.now();
    }

    // 활성화 상태로 변경
    public void activate() {
        this.isActive = true;
        // 재입장 시에도 lastEnteredAt을 업데이트
        this.lastEnteredAt = LocalDateTime.now();
    }

    // 비활성화 상태로 변경
    public void deactivate() {
        this.isActive = false;
        this.mateChatRoom.decrementCurrentMembers();
    }
}
