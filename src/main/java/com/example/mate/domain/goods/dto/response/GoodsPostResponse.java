package com.example.mate.domain.goods.dto.response;

import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.entity.Category;
import com.example.mate.domain.goods.entity.Role;
import com.example.mate.domain.goods.entity.Status;
import com.example.mate.domain.goods.vo.Location;
import com.example.mate.domain.goods.vo.MemberInfo;
import com.example.mate.domain.members.entity.Team;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;
import java.util.stream.Stream;
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
    private final Location location;
    private final List<String> imageUrls;
    private final Status status;

    /*
    굿즈 거래글 등록 요청을 GoodsPostResponse로 반환
    거래글 id와 회원정보 MemberInfo는 하드코딩
    request와 files 값에 따른 반환 값 확인
     */
    public static GoodsPostResponse of(GoodsPostRequest request, MultipartFile[] files) {
        return GoodsPostResponse.builder()
                .id(1L)
                .seller(MemberInfo.builder()
                        .memberId(1L)
                        .nickname("tester")
                        .manner(0.3F)
                        .role(Role.SELLER)
                        .build())
                .teamName(request.getTeamName())
                .title(request.getTitle())
                .category(request.getCategory().getValue())
                .price(request.getPrice())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrls(upload(files))
                .status(Status.OPEN)
                .build();
    }

    // 업로드한 이미지 파일명 리스트 반환
    public static List<String> upload(MultipartFile[] files) {
        // files가 null인 경우 빈 리스트 반환
        if (files == null || files.length == 0) {
            return List.of();
        }

        return Stream.of(files)
                .filter(file -> file.getContentType() != null && file.getContentType()
                        .startsWith("image/")) // 이미지 파일만 필터링
                .map(file -> UUID.randomUUID() + "_" + file.getOriginalFilename()) // 파일명 생성
                .collect(Collectors.toList());
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
                .teamName(Team.NC.getValue())
                .title("NC 다이노스 배틀크러쉬 모자")
                .category(Category.CLOTHING.getValue())
                .price(40000)
                .content("플레이스 홀더")
                .location(Location.builder()
                        .addressName("지번주소")
                        .placeName("장소명")
                        .roadAddressName("도로명주소")
                        .build())
                .imageUrls(List.of("upload/image1.png", "upload/image2.png", "upload/image3.png"))
                .status(Status.OPEN)
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
                .teamName(request.getTeamName())
                .title(request.getTitle())
                .category(request.getCategory().getValue())
                .price(request.getPrice())
                .content(request.getContent())
                .location(request.getLocation())
                .imageUrls(upload(files))
                .status(Status.OPEN)
                .build();
    }
}
