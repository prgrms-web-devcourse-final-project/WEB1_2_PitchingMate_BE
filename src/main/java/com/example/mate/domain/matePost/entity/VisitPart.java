package com.example.mate.domain.matePost.entity;

import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.*;
import lombok.*;


@Entity
@IdClass(VisitPartId.class)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class VisitPart {
    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "member_id")
    private Member member;

    @Id
    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "visit_id")
    private Visit visit;
}
