package com.example.mate.domain.mateChat.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.mate.entity.MatePost;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "mate_chat_room")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MateChatRoom extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "mate_post_id", nullable = false)
    private MatePost matePost;

    @Column(name = "active", nullable = false)
    @Builder.Default
    private Boolean active = true;

    @Column(name = "current_members", nullable = false)
    @Builder.Default
    private Integer currentMembers = 1;

    @OneToMany(mappedBy = "mateChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MateChatRoomMember> members = new ArrayList<>();

    @Column(name = "last_chat_content", columnDefinition = "TEXT")
    private String lastChatContent;

    @Column(name = "last_chat_sent_at")
    private LocalDateTime lastChatSentAt;

    public void updateLastChat(String content) {
        this.lastChatContent = content;
        this.lastChatSentAt = LocalDateTime.now();
    }

    public void incrementCurrentMembers() {
        if (this.currentMembers >= 10) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FULL);
        }
        this.currentMembers++;
    }

    public void decrementCurrentMembers() {
        this.currentMembers--;
        if (this.currentMembers == 0) {
            this.active = false;
        }
    }

    public void deactivate() {
        this.active = false;
    }
}