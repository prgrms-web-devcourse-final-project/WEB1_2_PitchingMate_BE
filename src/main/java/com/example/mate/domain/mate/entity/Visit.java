package com.example.mate.domain.mate.entity;

import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;

import java.util.ArrayList;
import java.util.List;

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

    @OneToMany(mappedBy = "visit")
    @Builder.Default
    private List<VisitPart> participants = new ArrayList<>();

    @OneToMany(mappedBy = "visit")
    @Builder.Default
    private List<MateReview> reviews = new ArrayList<>();

    public void detachPost() {
        this.post = null;
    }

    public void addParticipants(List<Member> members) {
        members.forEach(member -> {
            VisitPart visitPart = VisitPart.builder()
                    .member(member)
                    .visit(this)
                    .build();
            this.participants.add(visitPart);
        });
    }
}