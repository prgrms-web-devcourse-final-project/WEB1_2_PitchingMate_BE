package com.example.mate.domain.mateChat.dto.request;

import com.example.mate.domain.mateChat.dto.response.MateChatRoomResponse;
import lombok.Builder;
import lombok.Getter;

@Getter
@Builder
public class MateChatRoomJoinResult {
    private MateChatRoomResponse response;
    private boolean isFirstJoin;

    public static MateChatRoomJoinResult of(MateChatRoomResponse response, boolean isFirstJoin) {
        return MateChatRoomJoinResult.builder()
                .response(response)
                .isFirstJoin(isFirstJoin)
                .build();
    }
}
