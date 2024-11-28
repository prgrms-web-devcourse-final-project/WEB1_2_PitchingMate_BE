package com.example.mate.domain.goods.service;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;
import com.example.mate.common.utils.file.FileUploader;
import com.example.mate.common.utils.file.FileValidator;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.goods.dto.request.GoodsPostRequest;
import com.example.mate.domain.goods.dto.response.GoodsPostResponse;
import com.example.mate.domain.goods.entity.GoodsPost;
import com.example.mate.domain.goods.entity.GoodsPostImage;
import com.example.mate.domain.goods.repository.GoodsPostImageRepository;
import com.example.mate.domain.goods.repository.GoodsPostRepository;
import com.example.mate.domain.member.entity.Member;
import com.example.mate.domain.member.repository.MemberRepository;
import java.util.ArrayList;
import java.util.List;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
@Slf4j
public class GoodsService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsPostImageRepository imageRepository;

    public GoodsPostResponse registerGoodsPost(Long memberId, GoodsPostRequest request, List<MultipartFile> files) {
        // 사용자, 팀 정보, 이미지 파일 유효성 검증
        Member seller = memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));

        if (!TeamInfo.existById(request.getTeamId())) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }
        FileValidator.validateGoodsPostImages(files);

        // GoodsPost 엔티티 생성 & 저장
        GoodsPost goodsPost = GoodsPostRequest.toEntity(seller, request);
        GoodsPost savedPost = goodsPostRepository.save(goodsPost);

        // 이미지 저장 & 연관관계 설정
        List<GoodsPostImage> images = saveImages(files, savedPost);
        goodsPost.changeImages(images);

        return GoodsPostResponse.of(goodsPost);
    }

    private List<GoodsPostImage> saveImages(List<MultipartFile> files, GoodsPost savedPost) {
        List<GoodsPostImage> images = new ArrayList<>();
        for (MultipartFile file : files) {
            String uploadUrl = FileUploader.uploadFile(file);
            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl(uploadUrl)
                    .post(savedPost)
                    .build();
            GoodsPostImage savedImage = imageRepository.save(image);
            images.add(savedImage);
        }
        return images;
    }
}
