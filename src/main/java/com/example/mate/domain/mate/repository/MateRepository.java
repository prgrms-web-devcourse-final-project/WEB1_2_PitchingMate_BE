package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.entity.MatePost;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface MateRepository extends JpaRepository<MatePost, Long> {
}
