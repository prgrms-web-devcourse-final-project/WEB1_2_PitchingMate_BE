package com.example.mate.domain.member.entity;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.member.dto.request.JoinRequest;
import jakarta.persistence.*;
import lombok.*;

import java.time.LocalDate;
import java.util.HashMap;
import java.util.Map;

@Entity
@Table(name = "member")
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "nickname", length = 20, nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", length = 40, nullable = false, unique = true)
    private String email;

    @Column(name = "image_url", nullable = false)
    private String imageUrl;

    @Column(name = "age", nullable = false)
    private Integer age;

    @Enumerated(EnumType.STRING)
    @Column(name = "gender", nullable = false)
    private Gender gender;

    @Column(name = "team_id")
    private Long teamId;

    @Builder.Default
    @Column(name = "manner", nullable = false)
    private Float manner = 0.300F;

    @Column(name = "about_me", length = 100)
    private String aboutMe;

    @Enumerated(EnumType.STRING)
    @Column(name = "role", nullable = false)
    @Builder.Default
    private Role role = Role.USER;

    public void changeNickname(String nickname) {
        this.nickname = nickname;
    }

    public void changeImageUrl(String imageUrl) {
        this.imageUrl = imageUrl;
    }

    public void changeTeam(TeamInfo.Team team) {
        this.teamId = team != null ? team.id : null;
    }

    public void changeAboutMe(String aboutMe) {
        this.aboutMe = aboutMe;
    }

    public static Member of(JoinRequest request, String imageUrl) {
        return Member.builder()
                .name(request.getName())
                .nickname(request.getNickname())
                .email(request.getEmail())
                .imageUrl(imageUrl)
                .age(LocalDate.now().getYear() - Integer.parseInt(request.getBirthyear()))
                .gender(Gender.fromCode(request.getGender()))
                .teamId(request.getTeamId())
                .build();
    }

    // Jwt 문자의 내용 반환
    public Map<String, Object> getPayload() {
        Map<String, Object> payloadMap = new HashMap<>();
        payloadMap.put("memberId", id);
        payloadMap.put("name", name);
        payloadMap.put("nickname", nickname);
        payloadMap.put("email", email);
        payloadMap.put("role", role);
        return payloadMap;
    }
}
