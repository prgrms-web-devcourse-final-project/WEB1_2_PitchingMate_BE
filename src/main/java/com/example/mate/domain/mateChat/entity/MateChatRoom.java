package com.example.mate.domain.mateChat.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.mate.entity.MatePost;
import jakarta.persistence.*;
import lombok.*;

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
    private Integer currentMembers = 0;

    @OneToMany(mappedBy = "mateChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MateChatRoomMember> members = new ArrayList<>();

    @OneToMany(mappedBy = "mateChatRoom", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MateChatMessage> messages = new ArrayList<>();

    public static MateChatRoom create(MatePost matePost) {
        return MateChatRoom.builder()
                .matePost(matePost)
                .currentMembers(1)
                .build();
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