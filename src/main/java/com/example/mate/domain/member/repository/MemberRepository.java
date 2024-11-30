package com.example.mate.domain.member.repository;

import com.example.mate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    boolean existsByNickname(String nickname);
}
