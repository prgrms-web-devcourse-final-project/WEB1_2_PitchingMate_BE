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
        public final StadiumInfo.Stadium homeStadium;

        private Team(Long id, String shortName, String fullName, StadiumInfo.Stadium homeStadium) {
            this.id = id;
            this.shortName = shortName;
            this.fullName = fullName;
            this.homeStadium = homeStadium;
        }
    }

    public static final Team KIA = new Team(1L, "KIA", "KIA 타이거즈", StadiumInfo.GWANGJU);
    public static final Team LG = new Team(2L, "LG", "LG 트윈스", StadiumInfo.JAMSIL);
    public static final Team NC = new Team(3L, "NC", "NC 다이노스", StadiumInfo.CHANGWON);
    public static final Team SSG = new Team(4L, "SSG", "SSG 랜더스", StadiumInfo.INCHEON);
    public static final Team KT = new Team(5L, "KT", "kt wiz", StadiumInfo.SUWON);
    public static final Team DOOSAN = new Team(6L, "두산", "두산 베어스", StadiumInfo.JAMSIL);
    public static final Team LOTTE = new Team(7L, "롯데", "롯데 자이언츠", StadiumInfo.SAJIK);
    public static final Team SAMSUNG = new Team(8L, "삼성", "삼성 라이온즈", StadiumInfo.DAEGU);
    public static final Team KIWOOM = new Team(9L, "키움", "키움 히어로즈", StadiumInfo.GOCHEOK);
    public static final Team HANWHA = new Team(10L, "한화", "한화 이글스", StadiumInfo.DAEJEON);

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
}