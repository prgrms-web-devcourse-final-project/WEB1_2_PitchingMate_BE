package com.example.mate.domain.member.entity;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.Rating;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.member.dto.request.JoinRequest;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.Index;
import jakarta.persistence.Table;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.HashMap;
import java.util.Map;
import lombok.AccessLevel;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import org.hibernate.annotations.SQLDelete;

@Entity
@Table(
        name = "member",
        indexes = @Index(name = "idx_is_deleted", columnList = "is_deleted")
)
@Getter
@NoArgsConstructor(access = AccessLevel.PROTECTED)
@AllArgsConstructor(access = AccessLevel.PRIVATE)
@Builder
@SQLDelete(sql = "UPDATE member SET email = CONCAT(email, '.deleted.', CURRENT_TIMESTAMP), " +
        "nickname = CONCAT(nickname, '.deleted.', CURRENT_TIMESTAMP), is_deleted = true, " +
        "image_url = 'member_default.svg', deleted_at = CURRENT_TIMESTAMP WHERE id = ?")
public class Member {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "id")
    private Long id;

    @Column(name = "name", length = 10, nullable = false)
    private String name;

    @Column(name = "nickname", length = 50, nullable = false, unique = true)
    private String nickname;

    @Column(name = "email", length = 100, nullable = false, unique = true)
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

    @Column(name = "is_deleted", nullable = false)
    @Builder.Default
    private Boolean isDeleted = false;

    @Column(name = "deleted_at")
    private LocalDateTime deletedAt;

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

    public void updateManner(Rating rating) {
        if (rating == Rating.BAD) {
            this.manner = Math.max(0.0F, this.manner - 0.01F);
        } else if (rating == Rating.GOOD) {
            this.manner = Math.min(1.0F, this.manner + 0.02F);
        } else {
            this.manner = Math.min(1.0F, this.manner + 0.03F);
        }
    }

    public void updateManner(ActivityType activityType) {
        this.manner = Math.min(1.0F, this.manner + activityType.getValue());
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
