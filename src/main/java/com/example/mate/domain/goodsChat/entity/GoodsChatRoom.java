package com.example.mate.domain.goodsChat.entity;

import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goods_chat_room")
@Getter
@Builder
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
public class GoodsChatRoom {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id", nullable = false)
    private GoodsPost goodsPost;

    @Column(name = "last_chat_content", columnDefinition = "TEXT")
    private String lastChatContent;

    @Column(name = "last_chat_sent_at")
    private LocalDateTime lastChatSentAt;

    @Column(name = "is_active", nullable = false)
    @Builder.Default
    private Boolean isActive = true;

    @OneToMany(mappedBy = "goodsChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsChatPart> chatParts = new ArrayList<>();

    public void addChatParticipant(Member member, Role role) {
        GoodsChatPart chatPart = GoodsChatPart.builder()
                .goodsChatRoom(this)
                .member(member)
                .role(role)
                .build();

        chatParts.add(chatPart);
    }

    public void updateLastChat(String lastChatContent, LocalDateTime lastChatSentAt) {
        this.lastChatContent = lastChatContent;
        this.lastChatSentAt = lastChatSentAt;
    }


    public void deactivateRoom() {
        this.isActive = false;
    }

    public boolean isRoomActive() {
        return isActive;
    }
}