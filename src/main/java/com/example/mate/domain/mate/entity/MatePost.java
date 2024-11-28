package com.example.mate.domain.mate.entity;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.constant.Gender;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

@Entity
@Table(name = "mate_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MatePost {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "author_id", nullable = false)
    private Member author;

    @Column(name = "team_id", nullable = false)
    private Long teamId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "match_id", nullable = false)
    private Match match;

    @Column(name = "image_url")
    private String imageUrl;

    @Column(name = "title", length = 20, nullable = false)
    private String title;

    @Column(name = "content", length = 500, nullable = false)
    private String content;

    @Enumerated(EnumType.STRING)
    @Column(name = "status", nullable = false)
    private Status status;

    @Column(name = "max_participants", nullable = false)
    private Integer maxParticipants;

    @Column(name = "current_participants", nullable = false)
    @Builder.Default
    private Integer currentParticipants = 1;

    @Enumerated(EnumType.STRING)
    @Column(name = "age", nullable = false)
    private Age age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Enumerated(EnumType.STRING)
    @Column(name = "transport", nullable = false)
    private TransportType transport;

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL)
    private Visit visit;

    // Team 정보 조회
    public TeamInfo.Team getTeam() {
        return TeamInfo.getById(this.teamId);
    }

    // 게시글 전체 정보 수정
    public void updatePost(
            Long teamId,
            Match match,
            String imageUrl,
            String title,
            String content,
            Integer maxParticipants,
            Age age,
            Gender gender,
            TransportType transport
    ) {
        this.teamId = teamId;
        this.match = match;
        this.imageUrl = imageUrl;
        this.title = title;
        this.content = content;
        this.maxParticipants = maxParticipants;
        this.age = age;
        this.gender = gender;
        this.transport = transport;
    }

    // 모집 상태 변경
    public void changeStatus(Status status) {
        if (status == Status.COMPLETE) {
            throw new IllegalStateException("직관 완료는 completeVisit()을 통해서만 가능합니다.");
        }

        // 이미 직관 완료된 게시글은 상태 변경 불가
        if (this.status == Status.COMPLETE) {
            throw new IllegalStateException("직관 완료된 게시글은 상태를 변경할 수 없습니다.");
        }

        this.status = status;
    }

    // 직관 완료 처리
    public Visit completeVisit(List<Long> participantMemberIds) {
        if (this.status != Status.CLOSED) {
            throw new IllegalStateException("모집완료 상태에서만 직관 완료가 가능합니다.");
        }

        if (this.visit == null) {
            this.visit = Visit.builder()
                    .post(this)
                    .build();
        }

        this.status = Status.COMPLETE;
        return this.visit;
    }
}
