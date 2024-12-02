package com.example.mate.domain.match.repository;

import com.example.mate.domain.match.entity.TeamRecord;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface TeamRecordRepository extends JpaRepository<TeamRecord, Long> {
    List<TeamRecord> findAllByOrderByRankAsc();

    Optional<TeamRecord> findByTeamId(Long teamId);
}
