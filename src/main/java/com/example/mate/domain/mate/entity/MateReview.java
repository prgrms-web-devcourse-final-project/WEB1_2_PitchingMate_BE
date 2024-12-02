package com.example.mate.domain.mate.entity;

import com.example.mate.common.BaseTimeEntity;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "mate_review")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class MateReview extends BaseTimeEntity {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id", nullable = false)
    private Visit visit;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewer_id", nullable = false)
    private Member reviewer;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "reviewee_id", nullable = false)
    private Member reviewee;

    @Column(name = "review_content")
    private String reviewContent;

    @Enumerated(EnumType.STRING)
    @Column(name = "rating", nullable = false)
    private Rating rating;
}

