package com.example.mate.common.error;

import lombok.Getter;
import org.springframework.http.HttpStatus;

@Getter
public enum ErrorCode {
    // Common
    INTERNAL_SERVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "C001", "Internal Server Error"),
    INVALID_INPUT_VALUE(HttpStatus.BAD_REQUEST, "C002", "Invalid Input Value"),

    // Auth
    AUTH_BAD_REQUEST(HttpStatus.BAD_REQUEST, "A001", "잘못된 인증 요청입니다. 요청 파라미터를 확인해주세요."),
    AUTH_UNAUTHORIZED(HttpStatus.UNAUTHORIZED, "A002", "인증에 실패했습니다. 유효한 토큰을 제공해주세요."),
    AUTH_FORBIDDEN(HttpStatus.FORBIDDEN, "A003", "접근 권한이 없습니다. 권한을 확인해주세요."),

    // Team
    TEAM_NOT_FOUND(HttpStatus.NOT_FOUND, "T001", "팀을 찾을 수 없습니다"),

    // Stadium
    STADIUM_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "S001", "해당 ID의 경기장 정보를 찾을 수 없습니다"),
    STADIUM_NOT_FOUND_BY_NAME(HttpStatus.NOT_FOUND, "S002", "해당 이름의 경기장 정보를 찾을 수 없습니다"),

    // Match
    MATCH_NOT_FOUND(HttpStatus.NOT_FOUND, "M001", "해당 경기를 찾을 수 없습니다"),
    MATCH_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "MATCH_001", "해당 ID의 경기 정보를 찾을 수 없습니다."),

    // Member
    MEMBER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "M001", "해당 ID의 회원 정보를 찾을 수 없습니다"),

    // Follow
    ALREADY_FOLLOWED_MEMBER(HttpStatus.BAD_REQUEST, "F001", "이미 팔로우한 회원입니다."),
    ALREADY_UNFOLLOWED_MEMBER(HttpStatus.BAD_REQUEST, "F002", "이미 언팔로우한 회원입니다."),
    FOLLOWER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "F003", "해당 ID의 팔로워 회원을 찾을 수 없습니다."),
    FOLLOWING_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "F004", "해당 ID의 팔로잉 회원을 찾을 수 없습니다."),
    UNFOLLOWER_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "F005", "해당 ID의 언팔로워 회원을 찾을 수 없습니다."),
    UNFOLLOWING_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "F006", "해당 ID의 언팔로잉 회원을 찾을 수 없습니다."),

    // Mate Post
    INVALID_MATE_POST_PARTICIPANTS(HttpStatus.BAD_REQUEST, "MP001", "모집 인원은 2명에서 10명 사이여야 합니다."),
    INVALID_MATE_POST_STATUS_CHANGE(HttpStatus.BAD_REQUEST, "MP002", "직관 완료된 게시글은 상태를 변경할 수 없습니다."),
    INVALID_MATE_POST_COMPLETION(HttpStatus.BAD_REQUEST, "MP003", "모집완료 상태에서만 직관 완료가 가능합니다."),
    MATE_POST_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "MP004", "해당 ID의 메이트 게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_MATE_POST_ACCESS(HttpStatus.FORBIDDEN, "MP005", "해당 메이트 게시글에 대한 권한이 없습니다."),
    MATE_POST_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "MP006", "메이트 게시글의 작성자가 아니라면, 게시글을 수정할 수 없습니다"),
    ALREADY_COMPLETED_POST(HttpStatus.FORBIDDEN, "MP008", "이미 직관완료한 게시글은 모집 상태를 변경할 수 없습니다."),

    // Goods
    GOODS_IMAGES_ARE_EMPTY(HttpStatus.BAD_REQUEST, "G001", "굿즈 이미지는 최소 1개 이상을 업로드 할 수 있습니다."),
    GOODS_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "G002", "해당 ID의 굿즈 판매글 정보를 찾을 수 없습니다"),
    GOODS_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "G003", "판매글의 판매자가 아니라면, 판매글을 수정할 수 없습니다"),

    // FILE
    FILE_IS_EMPTY(HttpStatus.BAD_REQUEST, "F001", "빈 파일을 업로드할 수 없습니다. 파일 내용을 확인해주세요."),
    FILE_UNSUPPORTED_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "F002", "지원하지 않는 파일 형식입니다."),
    FILE_MISSING_MIME_TYPE(HttpStatus.UNSUPPORTED_MEDIA_TYPE, "F003", "파일의 MIME 타입을 찾을 수 없습니다."),
    FILE_UPLOAD_LIMIT_EXCEEDED(HttpStatus.BAD_REQUEST, "F004", "이미지 파일은 최대 10개까지 업로드 할 수 있습니다."),
    INVALID_FILE_TYPE(HttpStatus.BAD_REQUEST, "F005", "이미지 파일만 업로드 가능합니다."),
    FILE_UPLOAD_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F006", "파일 업로드에 실패했습니다."),
    FILE_DELETE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "F007", "파일 삭제에 실패했습니다."),

    // Age
    INVALID_AGE_VALUE(HttpStatus.BAD_REQUEST, "AGE001", "유효하지 않은 나이 값입니다."),

    // Gender
    INVALID_GENDER_VALUE(HttpStatus.BAD_REQUEST, "GENDER001", "유효하지 않은 성별 값입니다."),

    // TransportType
    INVALID_TRANSPORT_TYPE_VALUE(HttpStatus.BAD_REQUEST, "TRANSPORT001", "유효하지 않은 교통 수단 값입니다."),

    // SortType
    INVALID_SORT_TYPE_VALUE(HttpStatus.BAD_REQUEST, "ST001", "유효하지 않은 정렬 기준 값입니다."),

    // Status
    INVALID_STATUS_TYPE_VALUE(HttpStatus.BAD_REQUEST, "STATUS001", "유효하지 않은 모집 상태 값입니다.");


    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
