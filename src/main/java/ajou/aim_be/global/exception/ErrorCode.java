package ajou.aim_be.global.exception;

import lombok.AllArgsConstructor;
import lombok.Getter;

@Getter
@AllArgsConstructor
public enum ErrorCode {

    POST_NOT_FOUND(404, "게시글을 찾을 수 없습니다"),
    BOARD_MISMATCH(400, "게시판 정보가 일치하지 않습니다"),
    INVALID_BOARD_TYPE(400, "지원하지 않는 게시판 타입입니다."),
    NO_PERMISSION(403, "권한이 없습니다"),
    LOGIN_REQUIRED(401, "로그인이 필요합니다"),
    FIREBASE_INIT_FAILED(500, "Firebase 초기화에 실패했습니다."),

    FILE_UPLOAD_FAILED(500, "파일 업로드에 실패했습니다."),
    FILE_DELETE_FAILED(500, "파일 삭제에 실패했습니다."),
    INVALID_FILE(400, "유효하지 않은 파일입니다."),
    FILE_SIZE_EXCEEDED(400, "파일 크기 제한을 초과했습니다."),
    KEYWORD_LIMIT_EXCEEDED(400, "키워드는 최대 10개까지 선택할 수 있습니다."),
    INVALID_KEYWORD(400, "유효하지 않은 키워드입니다."),
    INVALID_ATTACHMENT(400, "유효하지 않은 첨부파일입니다."),
    ATTACHMENT_LIMIT_EXCEEDED(400, "첨부파일 개수 제한을 초과했습니다.(5개)"),

    INVALID_TOKEN(400, "유효하지 않은 토큰입니다."),
    INVALID_HEADER(400, "유효하지 않은 인증 헤더입니다."),
    ALREADY_APPROVED(400, "이미 승인된 요청입니다."),

    PORTFOLIO_WRITE_FORBIDDEN(403, "학생만 포트폴리오를 작성할 수 있습니다."),
    LAB_INTERN_WRITE_FORBIDDEN(403, "교수만 LAB_INTERN 게시글을 작성할 수 있습니다."),
    COMPANY_PROJECT_WRITE_FORBIDDEN(403, "승인된 기업만 프로젝트를 작성할 수 있습니다."),
    NOTICE_WRITE_FORBIDDEN(403, "관리자만 공지를 작성할 수 있습니다."),
    CRAWLED_PROJECT_WRITE_FORBIDDEN(403, "관리자만 크롤링 프로젝트를 생성할 수 있습니다."),

    POST_ACCESS_DENIED(403, "게시글에 접근할 권한이 없습니다."),
    POST_UPDATE_FORBIDDEN(403, "게시글을 수정할 권한이 없습니다."),
    POST_DELETE_FORBIDDEN(403, "게시글을 삭제할 권한이 없습니다."),

    INVALID_ROLE(400, "유효하지 않은 역할입니다."),
    INVALID_STATUS(400, "유효하지 않은 상태입니다."),
    INVALID_INPUT_VALUE(400, "입력값이 올바르지 않습니다."),
    USER_NOT_FOUND(404, "사용자를 찾을 수 없습니다."),
    UNAUTHORIZED(401, "인증이 필요합니다."),

    DUPLICATE_KEYWORD(409, "이미 존재하는 키워드입니다."),
    KEYWORD_NOT_FOUND(404, "키워드를 찾을 수 없습니다."),

    COMMENT_NOT_FOUND(404, "댓글을 찾을 수 없습니다."),
    INVALID_COMMENT(400, "유효하지 않은 댓글입니다.");


    private final int status;
    private final String message;
}