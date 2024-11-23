package com.example.mate.domain.member.dto.request;

import lombok.Getter;
import org.springframework.web.multipart.MultipartFile;

@Getter
public class MemberInfoUpdateRequest {

    private String nickname;
    private String aboutMe;
    private String myTeam;

    private MultipartFile image;
}
