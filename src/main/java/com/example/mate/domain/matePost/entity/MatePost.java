package com.example.mate.domain.matePost.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.common.error.CustomException;
import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.matePost.dto.request.MatePostUpdateRequest;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.List;

import static com.example.mate.common.error.ErrorCode.ALREADY_COMPLETED_POST;
import static com.example.mate.common.error.ErrorCode.DIRECT_VISIT_COMPLETE_FORBIDDEN;

@Entity
@Table(name = "mate_post")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MatePost extends BaseTimeEntity {
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

    @OneToOne(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    private Visit visit;

    // Team 정보 조회
    public TeamInfo.Team getTeam() {
        return TeamInfo.getById(this.teamId);
    }

    // 메이트 게시글 수정
    public void updatePost(MatePostUpdateRequest request, Match match, String imageUrl) {
        this.teamId = request.getTeamId();
        this.match = match;
        this.imageUrl = imageUrl;
        this.title = request.getTitle();
        this.content = request.getContent();
        this.maxParticipants = request.getMaxParticipants();
        this.age = request.getAge();
        this.gender = request.getGender();
        this.transport = request.getTransportType();
    }

    // 상태 변경 가능 여부 검증과 변경
    public void changeStatus(Status newStatus) {
        if (newStatus == Status.VISIT_COMPLETE) {
            throw new CustomException(DIRECT_VISIT_COMPLETE_FORBIDDEN);
        }

        if (this.status == Status.VISIT_COMPLETE) {
            throw new CustomException(ALREADY_COMPLETED_POST);
        }

        this.status = newStatus;
    }

    public void complete(List<Member> participants) {
        this.status = Status.VISIT_COMPLETE;
        this.visit = Visit.createForComplete(this, participants);
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }
}
