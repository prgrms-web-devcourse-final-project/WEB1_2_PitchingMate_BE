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
    UNAUTHORIZED_USER(HttpStatus.UNAUTHORIZED, "A004", "인증되지 않은 사용자입니다"),
    INVALID_AUTH_TOKEN(HttpStatus.BAD_REQUEST, "A005", "잘못된 토큰 형식입니다."),
    EXPIRED_AUTH_TOKEN(HttpStatus.BAD_REQUEST, "A006", "이미 만료된 토큰입니다."),

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
    UNSUPPORTED_RESPONSE_TYPE(HttpStatus.BAD_REQUEST, "M002", "회원 프로필 조회에서 지원하지 않는 응답 타입입니다."),
    ALREADY_USED_NICKNAME(HttpStatus.BAD_REQUEST, "M003", "이미 사용 중인 닉네임입니다."),
    MEMBER_NOT_FOUND_BY_EMAIL(HttpStatus.NOT_FOUND, "M004", "해당 이메일의 회원 정보를 찾을 수 없습니다."),
    MEMBER_UNAUTHORIZED_ACCESS(HttpStatus.FORBIDDEN, "M005", "해당 회원의 접근 권한이 없습니다."),
    MEMBER_AUTHENTICATION_REQUIRED(HttpStatus.BAD_REQUEST, "M006", "미리 인증된 회원의 정보가 필요합니다."),

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
    NOT_CLOSED_STATUS_FOR_COMPLETION(HttpStatus.BAD_REQUEST, "MP003", "모집완료 상태에서만 직관 완료가 가능합니다."),
    MATE_POST_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "MP004", "해당 ID의 메이트 게시글을 찾을 수 없습니다."),
    UNAUTHORIZED_MATE_POST_ACCESS(HttpStatus.FORBIDDEN, "MP005", "해당 메이트 게시글에 대한 권한이 없습니다."),
    MATE_POST_UPDATE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "MP006", "메이트 게시글의 작성자가 아니라면, 게시글을 수정할 수 없습니다"),
    DIRECT_VISIT_COMPLETE_FORBIDDEN(HttpStatus.FORBIDDEN, "MP007", "모집완료 상태가 아니라면 직관 완료 상태로 게시글을 수정할 수 없습니다"),
    MATE_POST_COMPLETE_TIME_NOT_ALLOWED(HttpStatus.FORBIDDEN, "MP008", "경기 시작 이후에만 직관 완료 처리가 가능합니다."),
    ALREADY_COMPLETED_POST(HttpStatus.FORBIDDEN, "MP009", "이미 직관완료한 게시글은 수정하거나 삭제할 수 없습니다."),
    MATE_POST_PARTICIPANTS_NOT_FOUND(HttpStatus.BAD_REQUEST, "MP010", "직관 참여자 목록이 비어있습니다."),
    VISIT_COMPLETE_POST_DELETE_FORBIDDEN(HttpStatus.FORBIDDEN, "MP0011", "직관 완료된 게시글은 삭제할 수 없습니다"),
    MATE_POST_MAX_PARTICIPANTS_EXCEEDED(HttpStatus.BAD_REQUEST, "MP0012", "방장을 포함한 참여자 수가 최대 모집 인원을 초과했습니다"),
    INVALID_MATE_POST_PARTICIPANT_IDS(HttpStatus.BAD_REQUEST, "MP013", "존재하지 않는 회원이 참여자 목록에 포함되어 있습니다"),

    // Goods
    GOODS_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "G001", "해당 ID의 굿즈 판매글 정보를 찾을 수 없습니다."),
    GOODS_MODIFICATION_NOT_ALLOWED(HttpStatus.FORBIDDEN, "G002", "판매글의 판매자가 아니라면, 판매글을 수정하거나 삭제할 수 없습니다."),
    GOODS_DELETE_NOT_ALLOWED(HttpStatus.BAD_REQUEST, "G003", "거래완료 상태에서 판매글을 삭제할 수 없습니다."),
    GOODS_ALREADY_COMPLETED(HttpStatus.BAD_REQUEST, "G004", "이미 거래완료 상태인 굿즈는 거래를 완료할 수 없습니다."),
    SELLER_CANNOT_BE_BUYER(HttpStatus.BAD_REQUEST, "G005", "판매자와 구매자는 동일할 수 없습니다."),

    // Goods Review
    GOODS_REVIEW_STATUS_NOT_CLOSED(HttpStatus.BAD_REQUEST, "GR001", "굿즈거래 후기는 거래완료 상태에서만 작성할 수 있습니다."),
    GOODS_REVIEW_NOT_ALLOWED_FOR_NON_BUYER(HttpStatus.FORBIDDEN, "GR002", "굿즈거래 후기는 구매자만 작성할 수 있습니다."),
    GOODS_REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "GR003", "굿즈거래 후기는 한 번만 작성할 수 있습니다."),

    // Goods Chat
    GOODS_CHAT_CLOSED_POST(HttpStatus.BAD_REQUEST, "GC001", "거래완료된 판매글에 채팅을 시작할 수 없습니다."),
    GOODS_CHAT_SELLER_CANNOT_START(HttpStatus.BAD_REQUEST, "GC002", "자신의 판매글에 채팅을 시작할 수 없습니다."),
    GOODS_CHAT_NOT_FOUND_CHAT_PART(HttpStatus.BAD_REQUEST, "GC003", "요청한 회원은 해당 채팅방에 참여한 회원이 아닙니다."),
    GOODS_CHAT_OPPONENT_NOT_FOUND(HttpStatus.NOT_FOUND, "GC004", "채팅방의 상대방 정보를 찾을 수 없습니다."),
    GOODS_CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "GC005", "해당 ID의 채팅방을 찾을 수 없습니다."),

    // Mate Review
    NOT_PARTICIPANT_OR_AUTHOR(HttpStatus.FORBIDDEN, "R002", "리뷰어와 리뷰 대상자 모두 직관 참여자여야 합니다."),
    REVIEW_NOT_FOUND_BY_ID(HttpStatus.NOT_FOUND, "R005", "해당 ID의 리뷰를 찾을 수 없습니다."),
    MATE_REVIEW_STATUS_NOT_VISIT_COMPLETE(HttpStatus.BAD_REQUEST, "MR003", "메이트 후기는 직관완료 상태에서만 작성할 수 있습니다."),
    MATE_REVIEW_ALREADY_EXISTS(HttpStatus.BAD_REQUEST, "MR004", "메이트 후기는 한 번만 작성할 수 있습니다."),

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
    INVALID_STATUS_TYPE_VALUE(HttpStatus.BAD_REQUEST, "STATUS001", "유효하지 않은 모집 상태 값입니다."),


    // Crawling
    CRAWLING_FAILED(HttpStatus.INTERNAL_SERVER_ERROR, "CR001", "크롤링 작업 중 오류가 발생했습니다"),
    CRAWLING_INTERRUPTED(HttpStatus.INTERNAL_SERVER_ERROR, "CR002", "크롤링 작업이 중단되었습니다"),
    WEBDRIVER_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CR003", "WebDriver 초기화 중 오류가 발생했습니다"),
    PARSING_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CR004", "데이터 파싱 중 오류가 발생했습니다"),
    MATCH_SAVE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CR005", "경기정보 저장 중 오류"),
    DATE_NOT_FOUND(HttpStatus.INTERNAL_SERVER_ERROR, "CR006", "날짜 정보를 찾을 수 없습니다"),
    TEAM_NAME_PARSE_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "CR007", "팀 이름 오류"),
    INVALID_SCORE_FORMAT(HttpStatus.INTERNAL_SERVER_ERROR, "CR008", "점수 형식이 잘못되었습니다"),

    // Rating
    INVALID_RATING_VALUE(HttpStatus.BAD_REQUEST, "Rating001", "유효하지 않은 리뷰 평가 값입니다."),

    //Weather
    WEATHER_DATA_NOT_FOUND(HttpStatus.NOT_FOUND, "W001", "날씨 데이터를 찾을 수 없습니다."),
    WEATHER_API_ERROR(HttpStatus.NOT_FOUND, "W002", "날씨 API 호출 중 오류가 발생했습니다."),

    // 채팅방 관련 에러
    CHAT_ROOM_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT001", "존재하지 않는 채팅방입니다."),
    CHAT_ROOM_MEMBER_NOT_FOUND(HttpStatus.NOT_FOUND, "CHAT002", "채팅방 멤버가 아닙니다."),
    CHAT_ROOM_FULL(HttpStatus.BAD_REQUEST, "CHAT003", "채팅방 인원이 가득 찼습니다. (최대 10명)"),
    ALREADY_JOINED_CHAT_ROOM(HttpStatus.BAD_REQUEST, "CHAT004", "이미 참여 중인 채팅방입니다."),
    CHAT_ROOM_CLOSED(HttpStatus.BAD_REQUEST, "CHAT005", "종료된 채팅방입니다."),
    CHAT_ROOM_ACCESS_DENIED(HttpStatus.FORBIDDEN, "CHAT006", "직관 완료된 채팅방에는 새로운 유저가 입장할 수 없습니다."),
    CHAT_AUTHOR_JOIN_DENIED(HttpStatus.FORBIDDEN, "CHAT006", "방장은 채팅방 퇴장 시 재입장할 수 없습니다."),
    AUTHOR_LEAVE_NOT_ALLOWED(HttpStatus.FORBIDDEN, "CHAT007", "방장은 직관완료가 안된 채팅방에서 나갈 수 없습니다."),
    CHAT_ROOM_NOT_MESSAGEABLE(HttpStatus.FORBIDDEN, "CHAT008", "메세지 전송이 불가능한 채팅방입니다."),

    // 채팅 참여 제한 관련 에러
    AGE_RESTRICTION_VIOLATED(HttpStatus.FORBIDDEN, "CHAT007", "연령 제한으로 입장할 수 없습니다."),
    GENDER_RESTRICTION_VIOLATED(HttpStatus.FORBIDDEN, "CHAT008", "성별 제한으로 입장할 수 없습니다."),

    // 채팅 기능 관련 에러
    CHAT_NOT_AVAILABLE(HttpStatus.BAD_REQUEST, "CHAT009", "2명 이상의 사용자가 있어야 채팅이 가능합니다."),
    MESSAGE_CONTENT_EMPTY(HttpStatus.BAD_REQUEST, "CHAT010", "메시지 내용을 입력해주세요."),
    INVALID_MESSAGE_TYPE(HttpStatus.BAD_REQUEST, "CHAT011", "잘못된 메시지 타입입니다."),

    // Notification
    NOTIFICATION_SEND_ERROR(HttpStatus.INTERNAL_SERVER_ERROR, "N001", "알림 전송에 실패했습니다."),
    NOTIFICATION_NOT_FOUND(HttpStatus.NOT_FOUND, "N002", "존재하지 않는 알림입니다."),
    INVALID_RECEIVER(HttpStatus.BAD_REQUEST, "N003", "알림 회원이 일치하지 않습니다.");

    private final HttpStatus status;
    private final String code;
    private final String message;

    ErrorCode(HttpStatus status, String code, String message) {
        this.status = status;
        this.code = code;
        this.message = message;
    }
}
