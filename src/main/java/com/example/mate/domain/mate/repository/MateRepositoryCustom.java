package com.example.mate.domain.mate.repository;

import com.example.mate.domain.mate.dto.request.MatePostSearchRequest;
import com.example.mate.domain.mate.entity.MatePost;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface MateRepositoryCustom {
    Page<MatePost> findMatePostsByFilter(MatePostSearchRequest request, Pageable pageable);
}
