package com.example.mate.domain.mateChat.dto.response;

import com.example.mate.domain.mateChat.entity.MateChatRoom;
import com.example.mate.domain.mateChat.entity.MateChatRoomMember;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MateChatRoomResponse {
    private Long roomId;
    private Long matePostId;
    private String matePostTitle;
    private String authorNickname;
    private Integer currentMembers;
    private Integer maxMembers;
    private LocalDateTime joinedAt;

    public static MateChatRoomResponse from(MateChatRoom chatRoom, MateChatRoomMember member) {
        return MateChatRoomResponse.builder()
                .roomId(chatRoom.getId())
                .matePostId(chatRoom.getMatePost().getId())
                .matePostTitle(chatRoom.getMatePost().getTitle())
                .authorNickname(chatRoom.getMatePost().getAuthor().getNickname())
                .currentMembers(chatRoom.getCurrentMembers())
                .maxMembers(10)  // 최대 인원 10명 고정
                .joinedAt(member.getCreatedAt())
                .build();
    }
}