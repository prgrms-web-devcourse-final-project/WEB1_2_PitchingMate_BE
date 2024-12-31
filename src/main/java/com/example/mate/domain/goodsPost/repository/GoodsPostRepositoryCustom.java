package com.example.mate.domain.goodsPost.repository;

import com.example.mate.domain.goodsPost.entity.Category;
import com.example.mate.domain.goodsPost.entity.GoodsPost;
import com.example.mate.domain.goodsPost.entity.Status;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;

public interface GoodsPostRepositoryCustom {
    Page<GoodsPost> findPageGoodsPosts(Long teamId, Status status, Category category, Pageable pageable);
}
