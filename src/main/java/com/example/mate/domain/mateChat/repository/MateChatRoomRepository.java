package com.example.mate.domain.mateChat.repository;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface MateChatRoomRepository extends JpaRepository<MateChatRoom, Long> {
    Optional<MateChatRoom> findByMatePostId(Long matePostId);
    boolean existsByMatePostId(Long matePostId);
}