package com.example.mate.domain.goods.dto.response;

import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.dto.LocationInfo;
import com.example.mate.domain.goods.dto.MemberInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import java.util.List;
import lombok.Builder;
import lombok.Getter;
import lombok.RequiredArgsConstructor;
import org.springframework.web.multipart.MultipartFile;

@Getter
@Builder
@RequiredArgsConstructor
public class GoodsPostResponse {

    private final Long id;
    private final MemberInfo seller;
    private final MemberInfo buyer;
    private final String teamName;
    private final String title;
    private final String category;
    private final Integer price;
    private final String content;
    private final LocationInfo location;
    private final List<String> imageUrls;
    private final String status;

    public static GoodsPostResponse of(GoodsPost goodsPost) {
        return GoodsPostResponse.builder()
                .id(goodsPost.getId())
                .seller(MemberInfo.from(goodsPost.getSeller(), Role.SELLER))
                .teamName(TeamInfo.getById(goodsPost.getTeamId()).shortName)
                .title(goodsPost.getTitle())
                .category(goodsPost.getCategory().getValue())
                .price(goodsPost.getPrice())
                .content(goodsPost.getContent())
                .location(LocationInfo.from(goodsPost.getLocation()))
                .imageUrls(goodsPost.getGoodsPostImages().stream().map(GoodsPostImage::getImageUrl).toList())
                .status(goodsPost.getStatus().getValue())
                .build();
    }

    /*
    굿즈 거래글 단건 조회 요청을 GoodsPostResponse로 반환
    거래글 id를 제외한 모든 값 하드코딩
     */
    public static GoodsPostResponse createResponse(Long id) {
        return GoodsPostResponse.builder()
                .id(id)
                .seller(MemberInfo.builder()
                        .memberId(1L)
                        .nickname("tester")
                        .manner(0.3F)
                        .role(Role.SELLER)
                        .build())
                .teamName(TeamInfo.NC.shortName)
                .title("NC 다이노스 배틀크러쉬 모자")
                .category(Category.CLOTHING.getValue())
                .price(40000)
                .content("플레이스 홀더")
                .location(LocationInfo.builder()
                        .addressName("지번주소")
                        .placeName("장소명")
                        .roadAddressName("도로명주소")
                        .build())
                .imageUrls(List.of("upload/image1.png", "upload/image2.png", "upload/image3.png"))
                .status(Status.OPEN.getValue())
                .build();
    }

    /*
    굿즈 거래글 수정 요청을 GoodsPostResponse로 반환
    회원정보 MemberInfo는 하드코딩
    id와 request 값에 따른 반환 값 확인
     */
    public static GoodsPostResponse updateResponse(Long id, GoodsPostRequest request, MultipartFile[] files) {
        return GoodsPostResponse.builder()
                .id(id)
                .seller(MemberInfo.builder()
                        .memberId(1L)
                        .nickname("tester")
                        .manner(0.3F)
                        .role(Role.SELLER)
                        .build())
                .teamName(getTeamName(request.getTeamId()))
                .title(request.getTitle())
                .category(request.getCategory().getValue())
                .price(request.getPrice())
                .content(request.getContent())
                .location(request.getLocation())
//                .imageUrls(upload(files))
                .status(Status.OPEN.getValue())
                .build();
    }

    // 요청 받은 teamId를 통해 해당 팀명 반환
    private static String getTeamName(Long teamId) {
        TeamInfo.Team team = TeamInfo.getById(teamId);
        return team.shortName;
    }
}
