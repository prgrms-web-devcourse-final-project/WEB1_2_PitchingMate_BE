package com.example.mate.domain.goods.entity;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.domain.member.entity.Member;
import jakarta.persistence.CascadeType;
import jakarta.persistence.Column;
import jakarta.persistence.Embedded;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.FetchType;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;
import jakarta.persistence.OneToMany;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.ToString;

@Entity
@Table(name = "goods_post")
@Getter
@Builder
@ToString
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoodsPost {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "seller_id", nullable = false)
    private Member seller;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "buyer_id")
    private Member buyer;

    @Column(name = "team_id")
    private Long teamId;

    @OneToMany(mappedBy = "post", cascade = CascadeType.ALL, orphanRemoval = true)
    @Builder.Default
    private List<GoodsPostImage> goodsPostImages = new ArrayList<>();

    @Column(nullable = false, length = 20)
    private String title;

    @Column(nullable = false, length = 500)
    private String content;

    @Column(nullable = false)
    private Integer price;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    private Category category;

    @Embedded
    private Location location;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false)
    @Builder.Default
    private Status status = Status.OPEN;

    // 굿즈 판매글 이미지 업로드 및 수정 메서드
    public void changeImages(List<GoodsPostImage> goodsPostImages) {
        if (goodsPostImages.isEmpty()) {
            throw new CustomException(ErrorCode.GOODS_IMAGES_ARE_EMPTY);
        }

        // 기존 이미지 전부 삭제
        this.goodsPostImages.clear();

        for (GoodsPostImage goodsPostImage : goodsPostImages) {
            this.goodsPostImages.add(goodsPostImage);
            goodsPostImage.changePost(this);
        }
    }

    // 굿즈 판매글 수정 메서드
    public void update(GoodsPost post) {
        this.teamId = post.getTeamId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.price = post.getPrice();
        this.category = post.getCategory();
        this.location = post.getLocation();
    }
}
