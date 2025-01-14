package com.example.mate.domain.matePost.dto.response;

import com.example.mate.domain.constant.Gender;
import com.example.mate.domain.constant.TeamInfo;
import com.example.mate.domain.file.FileUtils;
import com.example.mate.domain.match.entity.Match;
import com.example.mate.domain.matePost.entity.Age;
import com.example.mate.domain.matePost.entity.MatePost;
import com.example.mate.domain.matePost.entity.Status;
import com.example.mate.domain.matePost.entity.TransportType;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
public class MatePostDetailResponse {
    private String postImageUrl;
    private String title;
    private Status status;
    private String myTeamName;
    private String rivalTeamName;
    private LocalDateTime rivalMatchTime;
    private String location;
    private Age age;
    private Gender gender;
    private TransportType transportType;
    private Integer maxParticipants;
    private String userImageUrl;
    private String nickname;
    private Float manner;
    private String content;
    private Long postId;
    private Long matchId;
    private Long authorId;
    private Integer currentChatMembers;

    public static MatePostDetailResponse from(MatePost post, Integer currentChatMembers) {
        String myTeamName = TeamInfo.getById(post.getTeamId()).shortName;
        String rivalTeamName = getRivalTeamName(post);

        return MatePostDetailResponse.builder()
                .postImageUrl(FileUtils.getImageUrl(post.getImageUrl()))
                .title(post.getTitle())
                .status(post.getStatus())
                .myTeamName(myTeamName)
                .rivalTeamName(rivalTeamName)
                .rivalMatchTime(post.getMatch().getMatchTime())
                .location(post.getMatch().getStadium().name)
                .age(post.getAge())
                .gender(post.getGender())
                .transportType(post.getTransport())
                .maxParticipants(post.getMaxParticipants())
                .userImageUrl(FileUtils.getThumbnailImageUrl(post.getAuthor().getImageUrl()))
                .nickname(post.getAuthor().getNickname())
                .manner(post.getAuthor().getManner())
                .content(post.getContent())
                .postId(post.getId())
                .matchId(post.getMatch().getId())
                .authorId(post.getAuthor().getId())
                .currentChatMembers(currentChatMembers)
                .build();
    }

    private static String getRivalTeamName(MatePost post) {
        Match match = post.getMatch();
        Long postTeamId = post.getTeamId();

        if (postTeamId.equals(match.getHomeTeamId())) {
            return TeamInfo.getById(match.getAwayTeamId()).shortName;
        }
        else {
            return TeamInfo.getById(match.getHomeTeamId()).shortName;
        }
    }
}
