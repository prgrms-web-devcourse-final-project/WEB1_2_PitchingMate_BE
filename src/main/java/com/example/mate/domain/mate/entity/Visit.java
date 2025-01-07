package com.example.mate.domain.mate.entity;

import com.example.mate.domain.mate.dto.request.MateReviewCreateRequest;
import com.example.mate.domain.member.entity.ActivityType;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.OneToMany;
import jakarta.persistence.OneToOne;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "visit")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Visit {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @OneToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "post_id")
    private MatePost post;

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<VisitPart> participants = new ArrayList<>();

    @OneToMany(mappedBy = "visit", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<MateReview> reviews = new ArrayList<>();

    public static Visit createForComplete(MatePost post, List<Member> participants) {
        Visit visit = Visit.builder()
                .post(post)
                .build();

        // 참여자 정보 초기 설정
        participants.forEach(member -> {
            VisitPart visitPart = VisitPart.builder()
                    .member(member)
                    .visit(visit)
                    .build();
            visit.participants.add(visitPart);
            member.updateManner(ActivityType.MATE);
        });

        return visit;
    }

    public MateReview createReview(Member reviewer, Member reviewee, MateReviewCreateRequest request) {
        MateReview newMateReview = MateReview.builder()
                .visit(this)
                .reviewer(reviewer)
                .reviewee(reviewee)
                .reviewContent(request.getContent())
                .rating(request.getRating())
                .build();

        reviews.add(newMateReview);
        return newMateReview;
    }
}