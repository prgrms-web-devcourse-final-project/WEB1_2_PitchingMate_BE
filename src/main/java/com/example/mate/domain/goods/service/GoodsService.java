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
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.multipart.MultipartFile;

@Service
@Transactional
@RequiredArgsConstructor
public class GoodsService {

    private final MemberRepository memberRepository;
    private final GoodsPostRepository goodsPostRepository;
    private final GoodsPostImageRepository imageRepository;

    public GoodsPostResponse registerGoodsPost(Long memberId, GoodsPostRequest request, List<MultipartFile> files) {
        // 사용자, 팀 정보, 이미지 파일 유효성 검증
        Member seller = getSellerAndValidate(memberId);
        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);

        // GoodsPost 엔티티 생성 & 저장
        GoodsPost goodsPost = GoodsPostRequest.toEntity(seller, request);
        GoodsPost savedPost = goodsPostRepository.save(goodsPost);

        // 이미지 저장 & 연관관계 설정
        List<GoodsPostImage> images = saveImages(files, savedPost);
        goodsPost.changeImages(images);

        return GoodsPostResponse.of(goodsPost);
    }

    public GoodsPostResponse updateGoodsPost(Long memberId, Long goodsPostId, GoodsPostRequest request, List<MultipartFile> files) {
        // 사용자, 판매글 정보, 팀 정보, 이미지 파일 유효성 검증
        Member seller = getSellerAndValidate(memberId);
        GoodsPost goodsPost = getGoodsPostAndValidate(seller, goodsPostId);
        validateTeamInfo(request.getTeamId());
        FileValidator.validateGoodsPostImages(files);

        // 판매글 정보 업데이트
        GoodsPost updatedPost = GoodsPostRequest.toEntity(seller, request);
        goodsPost.update(updatedPost);
        deleteExistingImages(goodsPostId);

        // 이미지 업로드 & 저장
        List<GoodsPostImage> images = saveImages(files, goodsPost);
        goodsPost.changeImages(images);

        return GoodsPostResponse.of(goodsPost);
    }

    public void deleteGoodsPost(Long memberId, Long goodsPostId) {
        // 사용자, 판매글 정보 유효성 검증
        Member seller = getSellerAndValidate(memberId);
        GoodsPost goodsPost = getGoodsPostAndValidate(seller, goodsPostId);

        // 업로된 이미지 파일 삭제
        deleteExistingImages(goodsPostId);
        goodsPostRepository.delete(goodsPost);
    }

    @Transactional(readOnly = true)
    public GoodsPostResponse getGoodsPost(Long goodsPostId) {
        GoodsPost goodsPost = goodsPostRepository.findById(goodsPostId).orElseThrow(()
                -> new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));

        return GoodsPostResponse.of(goodsPost);
    }

    private Member getSellerAndValidate(Long memberId) {
        return memberRepository.findById(memberId).orElseThrow(()
                -> new CustomException(ErrorCode.MEMBER_NOT_FOUND_BY_ID));
    }

    private void validateTeamInfo(Long teamId) {
        if (!TeamInfo.existById(teamId)) {
            throw new CustomException(ErrorCode.TEAM_NOT_FOUND);
        }
    }

    private GoodsPost getGoodsPostAndValidate(Member seller, Long goodsPostId) {
        GoodsPost goodsPost = goodsPostRepository.findById(goodsPostId).orElseThrow(()
                -> new CustomException(ErrorCode.GOODS_NOT_FOUND_BY_ID));

        if (goodsPost.getSeller() != seller) {
            throw new CustomException(ErrorCode.GOODS_UPDATE_NOT_ALLOWED);
        }
        return goodsPost;
    }

    private void deleteExistingImages(Long goodsPostId) {
        List<String> imageUrls = imageRepository.getImageUrlsByPostId(goodsPostId);
        imageUrls.forEach(url -> {
            if (!FileUploader.deleteFile(url)) {
                throw new CustomException(ErrorCode.FILE_DELETE_ERROR);
            }
        });
        imageRepository.deleteAllByPostId(goodsPostId);
    }

    private List<GoodsPostImage> saveImages(List<MultipartFile> files, GoodsPost savedPost) {
        List<GoodsPostImage> images = new ArrayList<>();

        for (MultipartFile file : files) {
            String uploadUrl = FileUploader.uploadFile(file);
            GoodsPostImage image = GoodsPostImage.builder()
                    .imageUrl(uploadUrl)
                    .post(savedPost)
                    .build();
            images.add(image);
        }
        return images;
    }
}
