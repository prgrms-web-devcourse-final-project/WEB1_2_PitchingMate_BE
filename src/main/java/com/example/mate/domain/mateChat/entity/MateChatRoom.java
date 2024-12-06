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

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @Column(name = "is_messageable", nullable = false)
    @Builder.Default
    private Boolean isMessageable = true;

    @Column(name = "is_author_left", nullable = false)
    @Builder.Default
    private Boolean isAuthorLeft = false;

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

    public void setAuthorLeft(boolean authorLeft) {
        this.isAuthorLeft = authorLeft;
        if (authorLeft) {
            this.isMessageable = false;
        }
    }

    public void setMessageable(boolean messageable) {
        this.isMessageable = messageable;
    }

    public void deactivate() {
        this.isActive = false;
        this.isMessageable = false;
    }

    public void incrementCurrentMembers() {
        if (this.currentMembers >= 10) {
            throw new CustomException(ErrorCode.CHAT_ROOM_FULL);
        }
        this.currentMembers++;

        // 2명 이상이면 메시지 전송 가능하도록 설정
        if (this.currentMembers >= 2 && !this.isAuthorLeft) {
            this.isMessageable = true;
        }

        // 멤버가 있으면 채팅방 활성화
        if (this.currentMembers > 0) {
            this.isActive = true;
        }
    }

    public void decrementCurrentMembers() {
        if (this.currentMembers > 0) {
            this.currentMembers--;
        }

        // 1명 이하면 메시지 전송 불가능하도록 설정
        if (this.currentMembers <= 1) {
            this.isMessageable = false;
        }

        // 멤버가 없으면 채팅방 비활성화
        if (this.currentMembers == 0) {
            this.isActive = false;
            this.isMessageable = false;
        }
    }
}