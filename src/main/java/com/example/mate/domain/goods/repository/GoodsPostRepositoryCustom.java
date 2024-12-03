package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoodsPostRepositoryCustom {
    Page<GoodsPost> findPageGoodsPosts(Long teamId, Status status, Category category, Pageable pageable);
}
