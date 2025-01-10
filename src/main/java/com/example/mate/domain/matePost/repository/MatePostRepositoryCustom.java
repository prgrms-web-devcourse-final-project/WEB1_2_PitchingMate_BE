package com.example.mate.domain.matePost.repository;

import com.example.mate.domain.matePost.dto.request.MatePostSearchRequest;
import com.example.mate.domain.matePost.entity.MatePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MatePostRepositoryCustom {
    Page<MatePost> findMatePostsByFilter(MatePostSearchRequest request, Pageable pageable);
}
