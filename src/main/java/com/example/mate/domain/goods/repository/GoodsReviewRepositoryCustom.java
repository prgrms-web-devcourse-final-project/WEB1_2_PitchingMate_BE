package com.example.mate.domain.goods.repository;

import com.example.mate.domain.member.dto.response.MyReviewResponse;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoodsReviewRepositoryCustom {

    Page<MyReviewResponse> findGoodsReviewsByRevieweeId(Long memberId, Pageable pageable);
}
