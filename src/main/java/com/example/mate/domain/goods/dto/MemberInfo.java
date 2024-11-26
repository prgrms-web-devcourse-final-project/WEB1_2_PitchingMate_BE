package com.example.mate.domain.goods.dto;

import com.example.mate.domain.goods.entity.Role;
import lombok.AccessLevel;
import lombok.Builder;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.NoArgsConstructor;

@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@EqualsAndHashCode
public class MemberInfo {

    private Long memberId;
    private String nickname;
    private Float manner;
    private Role role;

    @Builder
    public MemberInfo(Long memberId, String nickname, Float manner, Role role) {
        this.memberId = memberId;
        this.nickname = nickname;
        this.manner = manner;
        this.role = role;
    }
}
