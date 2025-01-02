package com.example.mate.domain.member.repository;

import com.example.mate.domain.member.entity.Member;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MemberRepository extends JpaRepository<Member, Long> {

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.nickname = :nickname AND m.isDeleted = false")
    boolean existsByNickname(String nickname);

    @Query("SELECT CASE WHEN COUNT(m) > 0 THEN true ELSE false END FROM Member m WHERE m.email = :email AND m.isDeleted = false")
    boolean existsByEmail(String email);

    @Query("SELECT m FROM Member m WHERE m.email = :email AND m.isDeleted = false")
    Optional<Member> findByEmail(String email);
}
