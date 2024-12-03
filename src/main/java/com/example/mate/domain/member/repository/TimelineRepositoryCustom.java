package com.example.mate.domain.member.repository;

import com.example.mate.domain.member.dto.response.MyTimelineResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface TimelineRepositoryCustom {

    Page<MyTimelineResponse> findVisitsById(Long memberId, Pageable pageable);
}
