package com.example.mate.domain.matePost.repository;

import com.example.mate.domain.member.dto.response.MyReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MateReviewRepositoryCustom {

    Page<MyReviewResponse> findMateReviewsByRevieweeId(Long memberId, Pageable pageable);
}
