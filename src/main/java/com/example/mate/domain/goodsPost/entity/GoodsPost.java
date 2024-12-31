package com.example.mate.domain.goodsPost.entity;

import com.example.mate.common.BaseTimeEntity;
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
import jakarta.persistence.OrderBy;
import jakarta.persistence.Table;
import java.util.ArrayList;
import java.util.List;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Entity
@Table(name = "goods_post")
@Getter
@Builder
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@NoArgsConstructor(access = AccessLevel.PROTECTED)
public class GoodsPost extends BaseTimeEntity {

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
    @OrderBy("id ASC")
    @Builder.Default
    private List<GoodsPostImage> goodsPostImages = new ArrayList<>();

    @Column(name = "main_image_url", columnDefinition = "TEXT")
    private String mainImageUrl;

    @Column(nullable = false, length = 100)
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

    public void changeImages(List<GoodsPostImage> goodsPostImages) {
        this.goodsPostImages.clear();

        for (GoodsPostImage goodsPostImage : goodsPostImages) {
            this.goodsPostImages.add(goodsPostImage);
            goodsPostImage.changePost(this);
        }
        changeMainImage();
    }

    private void changeMainImage() {
        this.mainImageUrl = goodsPostImages.get(0).getImageUrl();
    }

    public void updatePostDetails(GoodsPost post) {
        this.teamId = post.getTeamId();
        this.title = post.getTitle();
        this.content = post.getContent();
        this.price = post.getPrice();
        this.category = post.getCategory();
        this.location = post.getLocation();
    }

    public void completeTransaction(Member buyer) {
        this.buyer = buyer;
        this.status = Status.CLOSED;
    }
}
