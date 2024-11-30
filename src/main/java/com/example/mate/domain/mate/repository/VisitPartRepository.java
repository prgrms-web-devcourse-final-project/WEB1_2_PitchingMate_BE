package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.VisitPart;
import com.example.mate.domain.mate.entity.VisitPartId;
import com.example.mate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;

public interface VisitPartRepository extends JpaRepository<VisitPart, VisitPartId> {

    int countByMember(Member member);
}
