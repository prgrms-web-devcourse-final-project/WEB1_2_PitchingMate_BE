package com.example.mate.domain.constant;

import com.example.mate.common.error.CustomException;
import com.example.mate.common.error.ErrorCode;

import java.util.List;
import lombok.Getter;

@Getter
public final class TeamInfo {

    public static final class Team {
        public final Long id;
        public final String shortName;
        public final String fullName;
        public final List<StadiumInfo.Stadium> homeStadiums;

        private Team(Long id, String shortName, String fullName, List<StadiumInfo.Stadium> homeStadiums) {
            this.id = id;
            this.shortName = shortName;
            this.fullName = fullName;
            this.homeStadiums = homeStadiums;
        }
        public List<StadiumInfo.Stadium> getHomeStadiums() {
            return homeStadiums;
        }
    }

    public static final Team KIA = new Team(1L, "KIA", "KIA 타이거즈", List.of(StadiumInfo.GWANGJU));
    public static final Team LG = new Team(2L, "LG", "LG 트윈스", List.of(StadiumInfo.JAMSIL));
    public static final Team NC = new Team(3L, "NC", "NC 다이노스", List.of(StadiumInfo.CHANGWON));
    public static final Team SSG = new Team(4L, "SSG", "SSG 랜더스", List.of(StadiumInfo.INCHEON));
    public static final Team KT = new Team(5L, "KT", "kt wiz", List.of(StadiumInfo.SUWON));
    public static final Team DOOSAN = new Team(6L, "두산", "두산 베어스", List.of(StadiumInfo.JAMSIL, StadiumInfo.ICHON_DOOSAN_BEARS_PARK));
    public static final Team LOTTE = new Team(7L, "롯데", "롯데 자이언츠", List.of(StadiumInfo.SAJIK, StadiumInfo.ULSAN));
    public static final Team SAMSUNG = new Team(8L, "삼성", "삼성 라이온즈", List.of(StadiumInfo.DAEGU));
    public static final Team KIWOOM = new Team(9L, "키움", "키움 히어로즈", List.of(StadiumInfo.GOCHEOK));
    public static final Team HANWHA = new Team(10L, "한화", "한화 이글스", List.of(StadiumInfo.DAEJEON,StadiumInfo.CHEONGJU));

    public static final List<Team> TEAMS = List.of(
            KIA, LG, NC, SSG, KT, DOOSAN, LOTTE, SAMSUNG, KIWOOM, HANWHA
    );

    public static Team getById(Long id) {
        return TEAMS.stream()
                .filter(team -> team.id.equals(id))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

    public static boolean existById(Long id) {
        return TEAMS.stream()
                .anyMatch(team -> team.id.equals(id));
    }

    public static Team findByFullName(String fullName) {
        return TEAMS.stream()
                .filter(team -> team.fullName.equals(fullName) || team.shortName.equalsIgnoreCase(fullName))
                .findFirst()
                .orElseThrow(() -> new CustomException(ErrorCode.TEAM_NOT_FOUND));
    }

}