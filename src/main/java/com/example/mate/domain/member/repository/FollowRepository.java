package com.example.mate.domain.member.repository;


import com.example.mate.domain.member.entity.Follow;
import com.example.mate.domain.member.entity.Member;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface FollowRepository extends JpaRepository<Follow, Long> {

    // 특정 회원의 팔로잉 수 카운트
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.follower.id = :followerId")
    int countByFollowerId(@Param("followerId") Long followerId);

    // 특정 회원의 팔로워 수 카운트
    @Query("SELECT COUNT(f) FROM Follow f WHERE f.following.id = :followingId")
    int countByFollowingId(@Param("followingId") Long followingId);

    // 특정 회원에 대한 팔로잉 여부 판단
    boolean existsByFollowerIdAndFollowingId(Long followerId, Long followingId);

    void deleteByFollowerIdAndFollowingId(Long followerId, Long followingId);

    // 특정 회원의 팔로잉 리스트
    @Query("SELECT f.following FROM Follow f WHERE f.follower.id = :followerId ORDER BY f.id DESC ")
    Page<Member> findFollowingsByFollowerId(@Param("followerId") Long followerId, Pageable pageable);
}