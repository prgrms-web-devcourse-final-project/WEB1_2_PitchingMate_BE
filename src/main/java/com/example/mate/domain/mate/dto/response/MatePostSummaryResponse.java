package com.example.mate.domain.mate.dto.response;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.mate.entity.Age;
import com.example.mate.domain.mate.entity.MatePost;
import com.example.mate.domain.mate.entity.Status;
import com.example.mate.domain.mate.entity.TransportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatePostSummaryResponse {

    private String imageUrl;
    private String title;
    private Status status;
    private String rivalTeamName;
    private LocalDateTime rivalMatchTime;
    private String location;
    private Integer maxParticipants;
    private Age age;
    private Gender gender;
    private TransportType transportType;

    public static MatePostSummaryResponse from(MatePost post) {
        return MatePostSummaryResponse.builder()
                .imageUrl(post.getImageUrl())
                .title(post.getTitle())
                .status(post.getStatus())
                .rivalTeamName(getRivalTeamName(post))
                .rivalMatchTime(post.getMatch().getMatchTime())
                .location(post.getMatch().getStadium().name)
                .maxParticipants(post.getMaxParticipants())
                .age(post.getAge())
                .gender(post.getGender())
                .transportType(post.getTransport())
                .build();
    }

    private static String getRivalTeamName(MatePost post) {
        Match match = post.getMatch();
        Long postTeamId = post.getTeamId(); // 게시글 작성자가 선택한 팀

        // 게시글 작성자가 선택한 팀이 홈팀인 경우 원정팀이 상대팀
        if (postTeamId.equals(match.getHomeTeamId())) {
            return TeamInfo.getById(match.getAwayTeamId()).shortName;
        }
        // 게시글 작성자가 선택한 팀이 원정팀인 경우 홈팀이 상대팀
        else {
            return TeamInfo.getById(match.getHomeTeamId()).shortName;
        }
    }
}
