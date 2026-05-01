package ajou.aim_be.comment.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;
import java.util.List;

@Builder
@Getter
public class CommentResponse {

    @Schema(description = "댓글 ID")
    private Long commentId;

    @Schema(description = "작성자 ID")
    private Long userId;

    @Schema(description = "댓글 내용")
    private String content;

    @Schema(description = "작성 시간")
    private LocalDateTime createdAt;

    @Schema(description = "삭제 여부")
    private boolean isDeleted;

    @Schema(description = "비공개 여부")
    private boolean isPrivate;

    @Schema(description = "공개 범위")
    private Visibility visibility;

    @Schema(description = "대댓글 목록")
    private List<CommentResponse> children;
}