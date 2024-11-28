package com.example.mate.domain.goods.entity;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class GoodsPostTest {

    @Test
    @DisplayName("굿즈 판매글 이미지를 업로드 할 수 있다.")
    void upload_post_Images() {
        // given
        GoodsPost goodsPost = GoodsPost.builder().title("title").price(10_000).build();
        GoodsPostImage image = GoodsPostImage.builder().imageUrl("test.jpg").build();
        GoodsPostImage image2 = GoodsPostImage.builder().imageUrl("test2.jpg").build();

        // when
        goodsPost.changeImages(List.of(image, image2));

        // then
        List<GoodsPostImage> actualPostImages = goodsPost.getGoodsPostImages();
        assertThat(actualPostImages).hasSize(2);
        assertThat(actualPostImages).containsExactlyInAnyOrder(image, image2);
        assertThat(actualPostImages.get(0).getIsMainImage()).isTrue();
        assertThat(actualPostImages.get(1).getIsMainImage()).isFalse();
        assertThat(image.getPost()).isEqualTo(goodsPost);
    }

    @Test
    @DisplayName("굿즈 판매글 이미지를 수정할 수 있다.")
    void change_post_Images() {
        // given
        GoodsPost goodsPost = GoodsPost.builder().title("title").price(10_000).build();
        GoodsPostImage image = GoodsPostImage.builder().imageUrl("test.jpg").build();
        GoodsPostImage image2 = GoodsPostImage.builder().imageUrl("test2.jpg").build();
        goodsPost.changeImages(List.of(image, image2));

        GoodsPostImage newImage = GoodsPostImage.builder().imageUrl("new.jpg").build();
        GoodsPostImage newImage2 = GoodsPostImage.builder().imageUrl("new2.jpg").build();

        // when
        goodsPost.changeImages(List.of(newImage, newImage2));

        // then
        List<GoodsPostImage> actualPostImages = goodsPost.getGoodsPostImages();
        assertThat(actualPostImages).hasSize(2);
        assertThat(actualPostImages).containsExactlyInAnyOrder(newImage, newImage2);
        assertThat(actualPostImages.get(0).getIsMainImage()).isTrue();
        assertThat(actualPostImages.get(1).getIsMainImage()).isFalse();
        assertThat(newImage.getPost()).isEqualTo(goodsPost);
    }

    @Test
    @DisplayName("비어 있는 이미지를 업로드하면 CustomException 이 발생한다.")
    void should_throw_CustomException_when_post_Images_are_null() {
        // given
        GoodsPost goodsPost = GoodsPost.builder().title("title").price(10_000).build();

        // when & then
        assertThatThrownBy(() -> goodsPost.changeImages(List.of()))
                .isInstanceOf(CustomException.class)
                .hasMessage(ErrorCode.GOODS_IMAGES_ARE_EMPTY.getMessage());
    }
}