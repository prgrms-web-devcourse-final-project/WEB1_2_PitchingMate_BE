package com.example.mate.domain.goods.repository;

import com.example.mate.domain.goods.entity.GoodsPostImage;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

public interface GoodsPostImageRepository extends JpaRepository<GoodsPostImage, Long> {

    @Modifying
    @Query("DELETE FROM GoodsPostImage g WHERE g.post.id = :postId")
    void deleteAllByPostId(@Param("postId") Long postId);

    @Query("SELECT g.imageUrl FROM GoodsPostImage g WHERE g.post.id = :postId")
    List<String> getImageUrlsByPostId(@Param("postId") Long postId);
}
