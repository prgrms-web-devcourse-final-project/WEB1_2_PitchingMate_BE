package com.example.mate.domain.mateChat.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

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

    private MateChatRoomMember(MateChatRoom mateChatRoom, Member member) {
        this.mateChatRoom = mateChatRoom;
        this.member = member;
    }

    public void deactivate() {
        this.isActive = false;
        this.mateChatRoom.decrementCurrentMembers();
    }
}
