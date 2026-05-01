package ajou.aim_be.board;

import io.swagger.v3.oas.annotations.media.Schema;

@Schema(description = "게시판 타입")
public enum BoardType {
    @Schema(description = "포트폴리오")
    PORTFOLIO,

    @Schema(description = "연구실 인턴")
    LAB_INTERN,

    @Schema(description = "기업 프로젝트")
    COMPANY_PROJECT,

    @Schema(description = "크롤링 프로젝트")
    CRAWLED_PROJECT,

    @Schema(description = "공지사항")
    NOTICE
}