package ajou.aim_be.comment.dto;

import ajou.aim_be.global.common.Visibility;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Builder;
import lombok.Getter;

import java.time.LocalDateTime;

@Getter
@Builder
@Schema(description = "댓글 생성 응답 DTO")
public class CommentCommandResponse {

    @Schema(description = "댓글 ID", example = "1")
    private Long commentId;

    @Schema(description = "게시글 ID", example = "100")
    private Long postId;

    @Schema(description = "부모 댓글 ID", example = "null", nullable = true)
    private Long parentCommentId;

    @Schema(description = "작성자 ID", example = "10")
    private Long userId;

    @Schema(description = "댓글 내용", example = "좋은 글 잘 봤습니다.")
    private String content;

    @Schema(description = "작성일시")
    private LocalDateTime createdAt;

    @Schema(description = "삭제 여부", example = "false")
    private boolean isDeleted;

    @Schema(description = "공개 범위", example = "PUBLIC")
    private Visibility visibility;

    @Schema(description = "변경 후 게시글 댓글 수", example = "4")
    private Long commentCount;
}
